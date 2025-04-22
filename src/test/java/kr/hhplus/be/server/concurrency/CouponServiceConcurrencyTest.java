package kr.hhplus.be.server.concurrency;

import static org.assertj.core.api.Assertions.*;
import static org.instancio.Select.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import kr.hhplus.be.server.domain.coupon.Coupon;
import kr.hhplus.be.server.domain.coupon.CouponRepository;
import kr.hhplus.be.server.domain.coupon.CouponService;
import kr.hhplus.be.server.domain.coupon.PublishedCoupon;
import kr.hhplus.be.server.domain.coupon.dto.CouponIssueCommand;
import kr.hhplus.be.server.domain.coupon.issuePolicy.CouponIssuePolicyType;
import kr.hhplus.be.server.infra.coupon.CouponJpaRepository;
import kr.hhplus.be.server.infra.coupon.PublishedCouponJpaRepository;
import kr.hhplus.be.server.support.IntgerationTestSupport;

public class CouponServiceConcurrencyTest extends IntgerationTestSupport {

	@Autowired
	private CouponService couponService;

	@Autowired
	private CouponJpaRepository couponJpaRepository;

	@Autowired
	private PublishedCouponJpaRepository publishedCouponJpaRepository;

	@Test
	@DisplayName("동시성 실패 테스트(따닥 문제) : 한 명의 사용자가 1개 남은 쿠폰 발급을 동시에 요청했을 때, 쿠폰 수량은 1개만 줄고 발급된 쿠폰은 2건이 생성")
	void couponConcurrenyFailTestDuplicateIssuance() throws InterruptedException {
		// given
		Coupon coupon = Instancio.of(Coupon.class)
			.ignore(field(Coupon.class, "id"))
			.set(field(Coupon.class, "issuePolicyType"), CouponIssuePolicyType.LIMITED)
			.set(field(Coupon.class, "remainingCount"), 1L)
			.create();
		coupon = couponJpaRepository.save(coupon);

		long userId = 123L;
		CouponIssueCommand command = new CouponIssueCommand(userId, coupon.getId());

		// when
		ExecutorService exec = Executors.newFixedThreadPool(2);
		CountDownLatch ready = new CountDownLatch(2);
		CountDownLatch start = new CountDownLatch(1);

		Runnable task = () -> {
			ready.countDown();
			try {
				start.await();
				couponService.issueCoupon(command);
			} catch (Exception ignored) {
			}
		};
		exec.submit(task);
		exec.submit(task);
		ready.await();
		start.countDown();
		exec.shutdown();
		exec.awaitTermination(1, TimeUnit.SECONDS);

		// then
		Coupon afterIssuedCoupon = couponJpaRepository.findById(coupon.getId()).orElseThrow();

		assertAll(
			() -> assertThat(afterIssuedCoupon.getRemainingCount()).isEqualTo(0L)
				.as("재고는 한 개가 차감되었다."),
			() -> assertThatThrownBy(
				() -> publishedCouponJpaRepository.findByUserIdAndCouponId(userId, afterIssuedCoupon.getId()))
				.isInstanceOf(IncorrectResultSizeDataAccessException.class)
				.as("한 명의 사용자에게 1개의 쿠폰은 한 번만 발급되어야 하는데 두 번 발급되었다.")
		);
	}

	@Test
	@DisplayName("동시성 제어 성공 테스트(비관적 락) : 한 명의 사용자가 동시에 쿠폰 발급을 요청 했을 때, 쿠폰 수량은 1개만 줄고 발급된 쿠폰은 1건이 생성")
	void couponConcurrencyTestWithPessimistic() throws InterruptedException {
		// given
		Coupon coupon = Instancio.of(Coupon.class)
			.ignore(field(Coupon.class, "id"))
			.set(field(Coupon.class, "issuePolicyType"), CouponIssuePolicyType.LIMITED)
			.set(field(Coupon.class, "remainingCount"), 1L)
			.create();
		coupon = couponJpaRepository.save(coupon);

		long userId = 123L;
		CouponIssueCommand command = new CouponIssueCommand(userId, coupon.getId());

		// when
		ExecutorService exec = Executors.newFixedThreadPool(2);
		CountDownLatch ready = new CountDownLatch(2);
		CountDownLatch start = new CountDownLatch(1);

		Runnable task = () -> {
			ready.countDown();
			try {
				start.await();
				couponService.issueCouponV2(command);
			} catch (Exception ignored) {
			}
		};
		exec.submit(task);
		exec.submit(task);
		ready.await();
		start.countDown();
		exec.shutdown();
		exec.awaitTermination(1, TimeUnit.SECONDS);

		// then
		Coupon afterIssuedCoupon = couponJpaRepository.findById(coupon.getId()).orElseThrow();

		assertAll(
			() -> assertThat(afterIssuedCoupon.getRemainingCount()).isEqualTo(0L)
				.as("재고는 한 개가 차감되었다."),
			() -> assertThat(publishedCouponJpaRepository.findByUserIdAndCouponId(userId, afterIssuedCoupon.getId()))
				.isNotNull()
				.as("한 명의 사용자에게 1개의 쿠폰만 발급되었다.")
		);
	}

	@Test
	@DisplayName("동시성 실패 테스트 : 선착순 쿠폰 50개에 서로 다른 100명이 동시 발급 요청을 보낼 경우, 쿠폰 수량은 50개가 차감되어야 하고 발급받은 유저는 50명이다.")
	void couponConcurrencyFailTestWithLimited() throws InterruptedException {
		// given
		Coupon coupon = Instancio.of(Coupon.class)
			.ignore(field(Coupon.class, "id"))
			.set(field(Coupon.class, "issuePolicyType"), CouponIssuePolicyType.LIMITED)
			.set(field(Coupon.class, "remainingCount"), 50L)
			.create();
		coupon = couponJpaRepository.save(coupon);
		Long couponId = coupon.getId();

		int totalUsers = 100;

		ExecutorService exec = Executors.newFixedThreadPool(totalUsers);
		CountDownLatch ready = new CountDownLatch(totalUsers);
		CountDownLatch start = new CountDownLatch(1);

		for (int i = 0; i < totalUsers; i++) {
			final long userId = i + 1L;
			exec.submit(() -> {
				ready.countDown();
				try {
					start.await();
					CouponIssueCommand command = new CouponIssueCommand(userId, couponId);
					couponService.issueCoupon(command);
				} catch (Exception ignored) {
				}
			});
		}

		ready.await();   // 모든 스레드가 준비될 때까지 대기
		start.countDown(); // 동시에 시작
		exec.shutdown();
		exec.awaitTermination(5, TimeUnit.SECONDS);

		// then
		Coupon after = couponJpaRepository.findById(coupon.getId()).orElseThrow();
		List<PublishedCoupon> allIssued = publishedCouponJpaRepository.findAll();

		assertAll(
			() -> assertThat(after.getRemainingCount()).isNotEqualTo(0L)
				.as("쿠폰 수량이 정확히 50개가 차감되지 않았다."),
			() -> assertThat(allIssued.size()).isNotEqualTo(50)
				.as("발급받은 사용자가 50명이 아니다.")
		);
	}

	@Test
	@DisplayName("동시성 제어 성공 테스트(비관적락) : 선착순 쿠폰 50개에 서로 다른 100명이 동시 발급 요청을 보낼 경우, 쿠폰 수량은 50개가 차감되어야 하고 발급받은 유저는 50명이다.")
	void couponConcurrencySuccessTestWithLimited() throws InterruptedException {
		// given
		Coupon coupon = Instancio.of(Coupon.class)
			.ignore(field(Coupon.class, "id"))
			.set(field(Coupon.class, "issuePolicyType"), CouponIssuePolicyType.LIMITED)
			.set(field(Coupon.class, "remainingCount"), 50L)
			.create();
		coupon = couponJpaRepository.save(coupon);
		Long couponId = coupon.getId();

		int totalUsers = 100;

		ExecutorService exec = Executors.newFixedThreadPool(totalUsers);
		CountDownLatch ready = new CountDownLatch(totalUsers);
		CountDownLatch start = new CountDownLatch(1);

		for (int i = 0; i < totalUsers; i++) {
			final long userId = i + 1L;
			exec.submit(() -> {
				ready.countDown();
				try {
					start.await();
					CouponIssueCommand command = new CouponIssueCommand(userId, couponId);
					couponService.issueCouponV2(command);
				} catch (Exception ignored) {
				}
			});
		}

		ready.await();   // 모든 스레드가 준비될 때까지 대기
		start.countDown(); // 동시에 시작
		exec.shutdown();
		exec.awaitTermination(1, TimeUnit.SECONDS);

		// then
		Coupon after = couponJpaRepository.findById(coupon.getId()).orElseThrow();
		List<PublishedCoupon> allIssued = publishedCouponJpaRepository.findAll();

		assertAll(
			() -> assertThat(after.getRemainingCount()).isEqualTo(0L)
				.as("쿠폰 수량이 정확히 50개 차감된다."),
			() -> assertThat(allIssued.size()).isEqualTo(50)
				.as("총 50명의 사용자만 발급받았다.")
		);
	}
}
