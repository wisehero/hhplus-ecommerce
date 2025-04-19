package kr.hhplus.be.server.concurrency;

import static org.assertj.core.api.Assertions.*;
import static org.instancio.Select.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.hhplus.be.server.domain.coupon.Coupon;
import kr.hhplus.be.server.domain.coupon.CouponRepository;
import kr.hhplus.be.server.domain.coupon.CouponService;
import kr.hhplus.be.server.domain.coupon.dto.CouponIssueCommand;
import kr.hhplus.be.server.domain.coupon.issuePolicy.CouponIssuePolicyType;
import kr.hhplus.be.server.infra.coupon.PublishedCouponJpaRepository;
import kr.hhplus.be.server.support.IntgerationTestSupport;

public class CouponServiceConcurrencyTest extends IntgerationTestSupport {

	@Autowired
	private CouponService couponService;

	@Autowired
	private CouponRepository couponRepository;

	@Autowired
	private PublishedCouponJpaRepository publishedCouponJpaRepository;

	@Test
	@DisplayName("한 명의 사용자가 동기에 쿠폰 발급을 요청해도 쿠폰은 1개만 생성된다.")
	void testConcurrentDuplicateIssuance() throws InterruptedException {
		// given
		Coupon coupon = Instancio.of(Coupon.class)
			.ignore(field(Coupon.class, "id"))
			.set(field(Coupon.class, "issuePolicyType"), CouponIssuePolicyType.LIMITED)
			.set(field(Coupon.class, "remainingCount"), 1L)
			.create();
		coupon = couponRepository.save(coupon);

		long userId = 123L;
		CouponIssueCommand cmd = new CouponIssueCommand(userId, coupon.getId());

		// when
		ExecutorService exec = Executors.newFixedThreadPool(2);
		CountDownLatch ready = new CountDownLatch(2);
		CountDownLatch start = new CountDownLatch(1);

		Runnable task = () -> {
			ready.countDown();
			try {
				start.await();
				couponService.issueCoupon(cmd);
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
		assertThat(publishedCouponJpaRepository.findAll().size()).isEqualTo(1);
	}
}
