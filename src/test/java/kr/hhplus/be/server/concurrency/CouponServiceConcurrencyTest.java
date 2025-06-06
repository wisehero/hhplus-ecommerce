package kr.hhplus.be.server.concurrency;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.*;
import static org.instancio.Select.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import kr.hhplus.be.server.domain.coupon.Coupon;
import kr.hhplus.be.server.domain.coupon.CouponService;
import kr.hhplus.be.server.domain.coupon.PublishedCoupon;
import kr.hhplus.be.server.domain.coupon.dto.CouponIssueCommand;
import kr.hhplus.be.server.domain.coupon.event.CouponEventPublisher;
import kr.hhplus.be.server.domain.coupon.issuePolicy.CouponIssuePolicyType;
import kr.hhplus.be.server.infra.coupon.CouponJpaRepository;
import kr.hhplus.be.server.infra.coupon.PublishedCouponJpaRepository;
import kr.hhplus.be.server.interfaces.consumer.CouponKafkaConsumer;
import kr.hhplus.be.server.support.IntgerationTestSupport;

public class CouponServiceConcurrencyTest extends IntgerationTestSupport {

	@Autowired
	private CouponService couponService;

	@Autowired
	private CouponJpaRepository couponJpaRepository;

	@Autowired
	private PublishedCouponJpaRepository publishedCouponJpaRepository;

	@MockitoSpyBean
	private CouponEventPublisher couponEventPublisher;

	@MockitoSpyBean
	private CouponKafkaConsumer couponKafkaConsumer;

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
			() -> assertThat(publishedCouponJpaRepository.findAll())
				.as("발급된 쿠폰 건수는 1개이다.")
		);
	}

	@Test
	@DisplayName("동시성 제어 성공 테스트(비관적 락) : 선착순 쿠폰 50개에 서로 다른 100명이 동시 발급 요청을 보낼 경우, 쿠폰 수량은 50개가 차감되어야 하고 발급받은 유저는 50명이다.")
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

	@Test
	@DisplayName("스핀 락 적용 : 10개 수량이 남은 쿠폰에 대해 20명이 쿠폰 발급을 요청했을 때 사용자 10명에게 쿠폰이 발급되고 10명은 쿠폰 발급을 받지 못한다.")
	void couponIssueSpinLockTest() throws InterruptedException {
		// given
		Coupon coupon = Instancio.of(Coupon.class)
			.ignore(field(Coupon.class, "id"))
			.set(field(Coupon.class, "issuePolicyType"), CouponIssuePolicyType.LIMITED)
			.set(field(Coupon.class, "remainingCount"), 10L)
			.create();
		coupon = couponJpaRepository.save(coupon);
		Long couponId = coupon.getId();

		int totalUsers = 20;

		ExecutorService exec = Executors.newFixedThreadPool(totalUsers);
		CountDownLatch ready = new CountDownLatch(totalUsers);
		CountDownLatch start = new CountDownLatch(1);

		AtomicInteger successCount = new AtomicInteger();
		AtomicInteger failCount = new AtomicInteger();

		for (int i = 0; i < totalUsers; i++) {
			final long userId = i + 1L;
			exec.submit(() -> {
				ready.countDown();
				try {
					start.await();
					CouponIssueCommand command = new CouponIssueCommand(userId, couponId);
					couponService.issueCouponV3(command);
					successCount.incrementAndGet();
				} catch (Exception ignored) {
					failCount.incrementAndGet();
				}
			});
		}

		ready.await();
		long startTime = System.currentTimeMillis(); // 시간 측정 시작
		start.countDown();
		exec.shutdown();
		exec.awaitTermination(3, TimeUnit.SECONDS);

		long elapsedTime = System.currentTimeMillis() - startTime;
		System.out.println("전체 요청 처리 시간(ms): " + elapsedTime);

		// then
		Coupon after = couponJpaRepository.findById(coupon.getId()).orElseThrow();
		List<PublishedCoupon> issued = publishedCouponJpaRepository.findAll();

		assertAll(
			() -> assertThat(successCount.get()).isEqualTo(10),
			() -> assertThat(failCount.get()).isEqualTo(10),
			() -> assertThat(after.getRemainingCount()).isEqualTo(0L),
			() -> assertThat(issued.size()).isEqualTo(10)
		);
	}

	@Test
	@DisplayName("Pub/sub 락 적용 : 10개 수량이 남은 쿠폰에 대해 20명이 쿠폰 발급을 요청했을 때 사용자 10명에게 쿠폰이 발급되고 10명은 쿠폰 발급을 받지 못한다.")
	void couponIssueDistriubtedLockTest() throws InterruptedException {
		// given
		Coupon coupon = Instancio.of(Coupon.class)
			.ignore(field(Coupon.class, "id"))
			.set(field(Coupon.class, "issuePolicyType"), CouponIssuePolicyType.LIMITED)
			.set(field(Coupon.class, "remainingCount"), 10L)
			.create();
		coupon = couponJpaRepository.save(coupon);
		Long couponId = coupon.getId();

		int totalUsers = 20;

		ExecutorService exec = Executors.newFixedThreadPool(totalUsers);
		CountDownLatch ready = new CountDownLatch(totalUsers);
		CountDownLatch start = new CountDownLatch(1);

		AtomicInteger successCount = new AtomicInteger();
		AtomicInteger failCount = new AtomicInteger();

		for (int i = 0; i < totalUsers; i++) {
			final long userId = i + 1L;
			exec.submit(() -> {
				ready.countDown();
				try {
					start.await();
					CouponIssueCommand command = new CouponIssueCommand(userId, couponId);
					couponService.issueCouponV4(command);
					successCount.incrementAndGet();
				} catch (Exception ignored) {
					failCount.incrementAndGet();
				}
			});
		}

		ready.await();

		long startTime = System.currentTimeMillis(); // 시간 측정 시작

		start.countDown();
		exec.shutdown();
		exec.awaitTermination(3, TimeUnit.SECONDS);

		long elapsedTime = System.currentTimeMillis() - startTime;
		System.out.println("전체 요청 처리 시간(ms): " + elapsedTime);

		// then
		Coupon after = couponJpaRepository.findById(coupon.getId()).orElseThrow();
		List<PublishedCoupon> issued = publishedCouponJpaRepository.findAll();

		assertAll(
			() -> assertThat(successCount.get()).isEqualTo(10),
			() -> assertThat(failCount.get()).isEqualTo(10),
			() -> assertThat(after.getRemainingCount()).isEqualTo(0L),
			() -> assertThat(issued.size()).isEqualTo(10)
		);
	}

	@Test
	@DisplayName("카프카 쿠폰 발급 : 쿠폰 10개에 사용자 20명 - 정확히 10개만 발급되어야 한다.")
	void couponIssueKafkaTest() throws InterruptedException {
		// given
		Coupon savedCoupon = couponJpaRepository.save(
			Instancio.of(Coupon.class)
				.ignore(field(Coupon.class, "id"))
				.set(field(Coupon.class, "issuePolicyType"), CouponIssuePolicyType.LIMITED)
				.set(field(Coupon.class, "remainingCount"), 10L)
				.create()
		);

		int userCount = 20;
		CountDownLatch startLatch = new CountDownLatch(1);
		CountDownLatch doneLatch = new CountDownLatch(userCount);
		ExecutorService executorService = Executors.newFixedThreadPool(userCount);

		List<CompletableFuture<Void>> futures = new ArrayList<>();
		List<Long> userIds = LongStream.rangeClosed(1, userCount).boxed().toList();

		// when
		for (Long userId : userIds) {
			CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
				try {
					startLatch.await();

					CouponIssueCommand command = new CouponIssueCommand(userId, savedCoupon.getId());
					couponService.issueRequest(command);

				} catch (Exception ignored) {
					// 예외 무시
				} finally {
					doneLatch.countDown();
				}
			}, executorService);

			futures.add(future);
		}

		startLatch.countDown();

		// 모든 요청 완료 대기
		boolean completed = doneLatch.await(10, TimeUnit.SECONDS);
		assertThat(completed).isTrue();

		// 카프카 처리 완료 대기
		await().atMost(30, TimeUnit.SECONDS)
			.untilAsserted(() -> {
				// 1. 쿠폰 재고가 0이 되었는지 확인
				Coupon finalCoupon = couponJpaRepository.findById(savedCoupon.getId()).orElseThrow();
				assertThat(finalCoupon.getRemainingCount()).isEqualTo(0L);

				// 2. 정확히 10개의 쿠폰만 발급되었는지 확인
				List<PublishedCoupon> publishedCoupons =
					publishedCouponJpaRepository.findAll();
				assertThat(publishedCoupons).hasSize(10);

				// 3. 발급받은 사용자 ID가 모두 다른지 확인
				List<Long> issuedUserIds = publishedCoupons.stream()
					.map(PublishedCoupon::getUserId)
					.collect(Collectors.toList());
				assertThat(issuedUserIds).hasSize(10);

				// 4. 발급받지 못한 사용자가 10명인지 확인
				Set<Long> allUserIds = LongStream.rangeClosed(1, 20)
					.boxed()
					.collect(Collectors.toSet());
				allUserIds.removeAll(issuedUserIds);
				assertThat(allUserIds).hasSize(10); // 10명은 발급받지 못함
			});
		executorService.shutdown();
	}
}
