package kr.hhplus.be.server.integration;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;

import kr.hhplus.be.server.domain.coupon.Coupon;
import kr.hhplus.be.server.domain.coupon.CouponRepository;
import kr.hhplus.be.server.domain.coupon.CouponService;
import kr.hhplus.be.server.domain.coupon.PublishedCoupon;
import kr.hhplus.be.server.domain.coupon.discountpolicy.DiscountType;
import kr.hhplus.be.server.domain.coupon.dto.CouponIssueCommand;
import kr.hhplus.be.server.domain.coupon.exception.CouponAlreadyIssuedException;
import kr.hhplus.be.server.domain.coupon.exception.CouponOutOfStockException;
import kr.hhplus.be.server.domain.coupon.issuePolicy.CouponIssuePolicyType;
import kr.hhplus.be.server.support.IntgerationTestSupport;

public class CouponServiceIntegrationTest extends IntgerationTestSupport {

	@Autowired
	private CouponService couponService;

	@Autowired
	private CouponRepository couponRepository;

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
			() -> assertThat(result.getCouponSnapshot().discountValue()).isEqualByComparingTo(BigDecimal.valueOf(1000)),
			() -> assertThat(result.getCouponSnapshot().discountType()).isEqualTo(DiscountType.FIXED)
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

		couponRepository.save(coupon);

		Long userId = 1L;
		CouponIssueCommand command = new CouponIssueCommand(userId, coupon.getId());

		// when
		couponService.issueCoupon(command);

		// then
		PublishedCoupon issued = couponRepository.findPublishedCouponBy(userId, coupon.getId());
		Coupon updatedCoupon = couponRepository.findById(coupon.getId());

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
}
