package kr.hhplus.be.server.integration;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.*;
import static org.instancio.Select.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import kr.hhplus.be.server.domain.coupon.Coupon;
import kr.hhplus.be.server.domain.coupon.CouponRepository;
import kr.hhplus.be.server.domain.coupon.CouponService;
import kr.hhplus.be.server.domain.coupon.PublishedCoupon;
import kr.hhplus.be.server.domain.coupon.discountpolicy.DiscountType;
import kr.hhplus.be.server.domain.coupon.dto.CouponIssueCommand;
import kr.hhplus.be.server.domain.coupon.event.CouponEventPublisher;
import kr.hhplus.be.server.domain.coupon.event.type.CouponIssueRequestEvent;
import kr.hhplus.be.server.domain.coupon.exception.CouponAlreadyIssuedException;
import kr.hhplus.be.server.domain.coupon.exception.CouponOutOfStockException;
import kr.hhplus.be.server.domain.coupon.issuePolicy.CouponIssuePolicyType;
import kr.hhplus.be.server.infra.coupon.PublishedCouponJpaRepository;
import kr.hhplus.be.server.infra.coupon.kafka.CouponKafkaProducer;
import kr.hhplus.be.server.infra.coupon.redis.CouponRedisRepository;
import kr.hhplus.be.server.interfaces.consumer.CouponKafkaConsumer;
import kr.hhplus.be.server.support.IntgerationTestSupport;

public class CouponServiceIntegrationTest extends IntgerationTestSupport {

	@Autowired
	private CouponService couponService;

	@Autowired
	private CouponRepository couponRepository;

	@Autowired
	private PublishedCouponJpaRepository publishedCouponJpaRepository;

	@MockitoSpyBean
	private CouponEventPublisher couponKafkaProducer;

	@MockitoSpyBean
	private CouponKafkaConsumer couponKafkaConsumer;

	@Test
	@DisplayName("발급된 쿠폰 ID로 조회하면 해당 쿠폰이 반환된다.")
	void shouldReturnPublishedCouponWhenIdIsValid() {
		// given
		Coupon coupon = Instancio.of(Coupon.class)
			.ignore(Select.field(Coupon.class, "id"))
			.set(Select.field(Coupon.class, "discountValue"), BigDecimal.valueOf(1000))
			.set(Select.field(Coupon.class, "discountType"), DiscountType.FIXED)
			.set(Select.field(Coupon.class, "issuePolicyType"), CouponIssuePolicyType.UNLIMITED)
			.set(Select.field(Coupon.class, "validFrom"), LocalDate.now().minusDays(1))
			.set(Select.field(Coupon.class, "validTo"), LocalDate.now().plusDays(7))
			.create();
		couponRepository.save(coupon);

		PublishedCoupon publishedCoupon = PublishedCoupon.create(
			100L, // userId
			coupon,
			LocalDate.now()
		);
		couponRepository.savePublishedCoupon(publishedCoupon);

		Long savedId = publishedCoupon.getId();

		// when
		PublishedCoupon result = couponService.getPublishedCouponById(savedId);

		// then
		assertAll(
			() -> assertThat(result.getId()).isEqualTo(savedId),
			() -> assertThat(result.getUserId()).isEqualTo(100L),
			() -> assertThat(result.getCouponSnapshot().getDiscountValue()).isEqualByComparingTo(
				BigDecimal.valueOf(1000)),
			() -> assertThat(result.getCouponSnapshot().getDiscountType()).isEqualTo(DiscountType.FIXED)
		);
	}

	@Test
	@DisplayName("발급된 쿠폰 ID가 null이면 예외가 발생한다.")
	void shouldThrowExceptionWhenPublishedCouponIdIsNull() {
		assertThatThrownBy(() -> couponService.getPublishedCouponById(null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("사용하려는 쿠폰 ID는 null일 수 없습니다.");
	}

	@Test
	@DisplayName("쿠폰을 발급하면 PublishedCoupon이 저장되고 잔여 수량이 감소한다.")
	void shouldIssueCouponAndDecreaseRemainingCount() {
		// given
		Coupon coupon = Instancio.of(Coupon.class)
			.ignore(Select.field(Coupon.class, "id"))
			.set(Select.field(Coupon.class, "couponName"), "테스트 쿠폰")
			.set(Select.field(Coupon.class, "discountValue"), BigDecimal.valueOf(3000))
			.set(Select.field(Coupon.class, "discountType"), DiscountType.FIXED)
			.set(Select.field(Coupon.class, "issuePolicyType"), CouponIssuePolicyType.LIMITED)
			.set(Select.field(Coupon.class, "remainingCount"), 5L)
			.set(Select.field(Coupon.class, "validFrom"), LocalDate.now().minusDays(1))
			.set(Select.field(Coupon.class, "validTo"), LocalDate.now().plusDays(5))
			.create();

		Coupon savedCoupon = couponRepository.save(coupon);

		Long userId = 1L;
		CouponIssueCommand command = new CouponIssueCommand(userId, savedCoupon.getId());

		// when
		couponService.issueCoupon(command);

		// then
		PublishedCoupon issued = couponRepository.findPublishedCouponBy(userId, savedCoupon.getId());
		Coupon updatedCoupon = couponRepository.findById(savedCoupon.getId());

		assertAll(
			() -> assertThat(issued.getUserId()).isEqualTo(userId),
			() -> assertThat(updatedCoupon.getRemainingCount()).isEqualTo(4L)
		);
	}

	@Test
	@DisplayName("이미 발급받은 유저가 다시 발급을 시도하면 CouponAlreadyIssuedException이 발생한다.")
	void shouldThrowExceptionWhenAlreadyIssued() {
		// given
		Coupon coupon = Instancio.of(Coupon.class)
			.ignore(Select.field(Coupon.class, "id"))
			.set(Select.field(Coupon.class, "couponName"), "중복 테스트 쿠폰")
			.set(Select.field(Coupon.class, "discountValue"), BigDecimal.valueOf(1000))
			.set(Select.field(Coupon.class, "discountType"), DiscountType.FIXED)
			.set(Select.field(Coupon.class, "issuePolicyType"), CouponIssuePolicyType.UNLIMITED)
			.set(Select.field(Coupon.class, "validFrom"), LocalDate.now().minusDays(1))
			.set(Select.field(Coupon.class, "validTo"), LocalDate.now().plusDays(5))
			.create();

		couponRepository.save(coupon);

		Long userId = 2L;

		PublishedCoupon publishedCoupon = PublishedCoupon.create(userId, coupon, LocalDate.now());
		couponRepository.savePublishedCoupon(publishedCoupon);

		CouponIssueCommand command = new CouponIssueCommand(userId, coupon.getId());

		// when & then
		assertThatThrownBy(() -> couponService.issueCoupon(command))
			.isInstanceOf(CouponAlreadyIssuedException.class);
	}

	@Test
	@DisplayName("LIMITED 정책의 쿠폰이 재고가 없으면 CouponOutOfStockException 예외가 발생하고 쿠폰은 발급되지 않는다.")
	void shouldThrowExceptionWhenLimitedCouponIsOutOfStock() {
		// given
		Coupon coupon = Instancio.of(Coupon.class)
			.ignore(Select.field(Coupon.class, "id"))
			.set(Select.field(Coupon.class, "couponName"), "품절 쿠폰")
			.set(Select.field(Coupon.class, "discountValue"), BigDecimal.valueOf(1000))
			.set(Select.field(Coupon.class, "discountType"), DiscountType.FIXED)
			.set(Select.field(Coupon.class, "issuePolicyType"), CouponIssuePolicyType.LIMITED)
			.set(Select.field(Coupon.class, "remainingCount"), 0L) // 핵심: 잔여 수량 0
			.set(Select.field(Coupon.class, "validFrom"), LocalDate.now().minusDays(1))
			.set(Select.field(Coupon.class, "validTo"), LocalDate.now().plusDays(5))
			.create();

		Coupon savedCoupon = couponRepository.save(coupon);

		Long userId = 10L;
		CouponIssueCommand command = new CouponIssueCommand(userId, savedCoupon.getId());

		// when & then
		assertThatThrownBy(() -> couponService.issueCoupon(command))
			.isInstanceOf(CouponOutOfStockException.class);

		// then
		assertThatThrownBy(() -> couponRepository.findPublishedCouponBy(userId, savedCoupon.getId()))
			.isInstanceOf(
				JpaObjectRetrievalFailureException.class) // EntityNotFoundException이 다시 JpaObjectRetrievalFailureException로 말아서 던져짐
			.hasMessageContaining("발행된 쿠폰을 찾을 수 없습니다");
	}

	@Test
	@DisplayName("이미 사용된 쿠폰을 복원하면 isUsed 값이 false로 바뀐다.")
	void shouldRestoreUsedPublishedCoupon() {
		// given
		Coupon coupon = Instancio.of(Coupon.class)
			.ignore(Select.field(Coupon.class, "id"))
			.set(Select.field(Coupon.class, "discountValue"), BigDecimal.valueOf(2000))
			.set(Select.field(Coupon.class, "discountType"), DiscountType.FIXED)
			.set(Select.field(Coupon.class, "issuePolicyType"), CouponIssuePolicyType.UNLIMITED)
			.set(Select.field(Coupon.class, "validFrom"), LocalDate.now().minusDays(1))
			.set(Select.field(Coupon.class, "validTo"), LocalDate.now().plusDays(3))
			.create();
		coupon = couponRepository.save(coupon);

		PublishedCoupon publishedCoupon = PublishedCoupon.create(123L, coupon, LocalDate.now());
		publishedCoupon.discount(BigDecimal.valueOf(10000), LocalDate.now()); // 쿠폰 사용 처리
		publishedCoupon = couponRepository.savePublishedCoupon(publishedCoupon);

		// when
		couponService.restorePublishedCoupon(publishedCoupon.getId());

		// then
		PublishedCoupon updated = couponRepository.findPublishedCouponById(publishedCoupon.getId());

		assertThat(updated.isUsed()).isFalse();
	}

	@Test
	@DisplayName("100개 쿠폰에 대해 200명의 사용자가 동시에 요청하면 정확히 100명만 발급받아야 한다")
	void shouldHandleConcurrentRequestsForLimitedCoupon() throws InterruptedException {
		// given
		Coupon coupon = Instancio.of(Coupon.class)
			.ignore(Select.field(Coupon.class, "id"))
			.set(Select.field(Coupon.class, "couponName"), "선착순 쿠폰")
			.set(Select.field(Coupon.class, "discountValue"), BigDecimal.valueOf(1000))
			.set(Select.field(Coupon.class, "discountType"), DiscountType.FIXED)
			.set(Select.field(Coupon.class, "issuePolicyType"), CouponIssuePolicyType.LIMITED)
			.set(Select.field(Coupon.class, "remainingCount"), 100L) // 100개 제한 쿠폰
			.set(Select.field(Coupon.class, "validFrom"), LocalDate.now().minusDays(1))
			.set(Select.field(Coupon.class, "validTo"), LocalDate.now().plusDays(5))
			.create();

		Coupon savedCoupon = couponRepository.save(coupon);
		Long couponId = savedCoupon.getId();

		int threadCount = 200; // 200명의 사용자
		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
		CountDownLatch latch = new CountDownLatch(threadCount);
		AtomicInteger successCount = new AtomicInteger(0);
		AtomicInteger failCount = new AtomicInteger(0);

		// when
		for (int i = 0; i < threadCount; i++) {
			final long userId = i + 1;
			executorService.submit(() -> {
				try {
					couponService.issueWithRedis(new CouponIssueCommand(userId, couponId));
					successCount.incrementAndGet();
				} catch (CouponAlreadyIssuedException e) {
					failCount.incrementAndGet(); // 중복 발급자가 없다.
				} catch (Exception e) {
					// 무시
				} finally {
					latch.countDown();
				}
			});
		}

		// 모든 요청 완료 대기
		latch.await();
		executorService.shutdown();

		// 스케줄러 실행 대기 (쿠폰 발급 처리 시간)
		Thread.sleep(5000);

		// then
		// 1. 큐에 추가 성공한 수는 200명이어야 함 (실패 케이스 없음)
		assertThat(successCount.get()).isEqualTo(threadCount);
		assertThat(failCount.get()).isEqualTo(0);

		// 2. 실제 발급된 쿠폰 수는 100개여야 함 (재고 한도)
		int actualIssuedCount = publishedCouponJpaRepository.findAll().size();
		assertThat(actualIssuedCount).isEqualTo(100);

		// 3. 업데이트된 쿠폰의 재고는 0이어야 함
		Coupon updatedCoupon = couponRepository.findById(couponId);
		assertThat(updatedCoupon.getRemainingCount()).isEqualTo(0);
	}

	@Test
	@DisplayName("쿠폰 발급 시도 요청이 발생하면 카프카로 메세지가 전달되고 쿠폰 원장의 개수가 차감되고 발급된 쿠폰이 생성된다.")
	void couponIssueKafkaTest() {
		// given
		Coupon savedCoupon = couponRepository.save(
			Instancio.of(Coupon.class)
				.ignore(field(Coupon.class, "id"))
				.set(field(Coupon.class, "issuePolicyType"), CouponIssuePolicyType.LIMITED)
				.set(field(Coupon.class, "remainingCount"), 10L)
				.create()
		);

		CouponIssueCommand command = new CouponIssueCommand(1L, savedCoupon.getId());

		// when
		couponService.issueRequest(command);

		// then
		await().atMost(10, TimeUnit.SECONDS)
			.untilAsserted(() -> {
				// 1. 카프카 프로듀서 호출 검증
				verify(couponKafkaProducer).publish(any(CouponIssueRequestEvent.class));

				// 2. 카프카 컨슈머 호출 검증
				verify(couponKafkaConsumer).consume(any(CouponIssueRequestEvent.class),
					anyString(), anyInt(), anyString());

				// 쿠폰 재고 차감 검증
				Coupon updatedCoupon = couponRepository.findById(savedCoupon.getId());
				assertThat(updatedCoupon.getRemainingCount()).isEqualTo(9L);

				// 발급된 쿠폰 테이블 저장 검증
				PublishedCoupon publishedCoupon = couponRepository.findPublishedCouponBy(1L, savedCoupon.getId());
				assertNotNull(publishedCoupon);
				assertThat(publishedCoupon.getUserId()).isEqualTo(1L);
			});

	}
}
