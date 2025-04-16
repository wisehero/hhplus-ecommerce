package kr.hhplus.be.server.domain.coupon;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import kr.hhplus.be.server.domain.coupon.discountpolicy.DiscountType;
import kr.hhplus.be.server.domain.coupon.exception.CouponAlreadyUsedException;
import kr.hhplus.be.server.domain.coupon.exception.CouponDoesNotUsedException;
import kr.hhplus.be.server.domain.coupon.exception.CouponExpiredException;
import kr.hhplus.be.server.domain.coupon.issuePolicy.CouponIssuePolicyType;

class PublishedCouponTest {

	@Test
	@DisplayName("쿠폰 발급 시 스냅샷과 발급 정보가 정확히 설정된다.")
	void createPublishedCouponShouldInitializeFieldsCorrectly() {
		// given
		Long userId = 123L;
		Long couponId = 999L;
		LocalDate issuedAt = LocalDate.of(2025, 4, 1);

		Coupon coupon = Instancio.of(Coupon.class)
			.set(Select.field("id"), couponId)
			.set(Select.field("couponName"), "회원가입 감사 쿠폰")
			.set(Select.field("discountType"), DiscountType.FIXED)
			.set(Select.field("discountValue"), BigDecimal.valueOf(3000))
			.set(Select.field("validFrom"), LocalDate.of(2025, 4, 1))
			.set(Select.field("validTo"), LocalDate.of(2025, 4, 30))
			.set(Select.field("issuePolicyType"), CouponIssuePolicyType.LIMITED)
			.create();

		CouponSnapshot expectedSnapshot = coupon.toSnapShot();

		// when
		PublishedCoupon publishedCoupon = PublishedCoupon.create(userId, coupon, issuedAt);

		// then
		assertAll(
			() -> assertThat(publishedCoupon).isNotNull(),
			() -> assertThat(publishedCoupon.getUserId()).isEqualTo(userId),
			() -> assertThat(publishedCoupon.getCouponId()).isEqualTo(couponId),
			() -> assertThat(publishedCoupon.getCouponSnapshot()).isEqualTo(expectedSnapshot),
			() -> assertThat(publishedCoupon.isUsed()).isFalse(),
			() -> assertThat(publishedCoupon.getIssuedAt()).isEqualTo(issuedAt)
		);
	}

	@Test
	@DisplayName("정상적인 할인 적용 시 할인 금액이 계산되고 사용 처리된다.")
	void applyDiscount() {
		// given
		Coupon coupon = Coupon.createUnlimited(
			"웰컴쿠폰",
			BigDecimal.valueOf(3000),
			DiscountType.FIXED,
			LocalDate.of(2025, 4, 1),
			LocalDate.of(2025, 4, 30)
		);

		LocalDate issuedAt = LocalDate.of(2025, 4, 10);
		PublishedCoupon published = PublishedCoupon.create(1L, coupon, issuedAt);

		BigDecimal originalPrice = BigDecimal.valueOf(10000);

		// when
		BigDecimal discounted = published.discount(originalPrice, LocalDate.now());

		// then
		assertAll(
			() -> assertThat(discounted).isEqualByComparingTo("7000"),
			() -> assertThat(published.isUsed()).isTrue()
		);
	}

	@Test
	@DisplayName("이미 사용된 쿠폰이라면 CouponAlreadyUsedException 발생")
	void couponAlreadyUsedException() {
		// given
		Coupon coupon = Coupon.createUnlimited(
			"웰컴쿠폰",
			BigDecimal.valueOf(3000),
			DiscountType.FIXED,
			LocalDate.of(2025, 4, 1),
			LocalDate.of(2025, 4, 30)
		);
		LocalDate issuedAt = LocalDate.of(2025, 4, 10);
		PublishedCoupon published = PublishedCoupon.create(1L, coupon, issuedAt);

		// when - first use
		published.discount(BigDecimal.valueOf(10000), LocalDate.now());

		// then - second use
		assertThatThrownBy(() -> published.discount(BigDecimal.valueOf(10000), LocalDate.now()))
			.isInstanceOf(CouponAlreadyUsedException.class)
			.hasMessage("비즈니스 정책을 위반한 요청입니다.")
			.extracting("detail")
			.isEqualTo("쿠폰이 이미 사용되었습니다.");
	}

	@Test
	@DisplayName("할인을 적용해야하는 가격이 null이거나 0 이하일 경우 IllegalArgumentException 발생")
	void discount_invalidPrice_shouldThrow() {
		// given
		Coupon coupon = Coupon.createUnlimited(
			"웰컴쿠폰",
			BigDecimal.valueOf(3000),
			DiscountType.FIXED,
			LocalDate.of(2025, 4, 1),
			LocalDate.of(2025, 4, 30)
		);
		LocalDate issuedAt = LocalDate.of(2025, 4, 10);
		PublishedCoupon published = PublishedCoupon.create(1L, coupon, issuedAt);

		// when & then
		assertAll(
			() -> assertThatThrownBy(() -> published.discount(null, LocalDate.now()))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("가격이 null이거나 0 이하입니다."),
			() -> assertThatThrownBy(() -> published.discount(BigDecimal.ZERO, LocalDate.now()))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("가격이 null이거나 0 이하입니다.")
		);
	}

	@Test
	@DisplayName("유효기간 내에 사용하지 않는다면 CouponExpiredException 발생")
	void discount_issuedOutOfValidity_shouldThrow() {
		// given
		Coupon coupon = Coupon.createUnlimited(
			"유효기간 쿠폰",
			BigDecimal.valueOf(2000),
			DiscountType.FIXED,
			LocalDate.of(2025, 4, 1),
			LocalDate.of(2025, 4, 30)
		);
		LocalDate issuedAt = LocalDate.of(2025, 4, 10);
		PublishedCoupon published = PublishedCoupon.create(1L, coupon, issuedAt);
		LocalDate now = LocalDate.of(2025, 5, 1); // 유효기간이 지난 날짜

		// when & then
		assertThatThrownBy(() -> published.discount(BigDecimal.valueOf(10000), now))
			.isInstanceOf(CouponExpiredException.class)
			.hasMessage("비즈니스 정책을 위반한 요청입니다.")
			.extracting("detail")
			.isEqualTo("쿠폰이 만료되었습니다. 유효 기간은 %s까지 입니다.".formatted(coupon.toSnapShot().getValidTo()));
	}

	@Test
	@DisplayName("사용된 쿠폰을 restore() 하면 isUsed 상태가 false로 변경된다.")
	void restore_usedCoupon_shouldSetIsUsedFalse() {
		// given
		Coupon coupon = Coupon.createUnlimited(
			"테스트 쿠폰",
			BigDecimal.valueOf(5000),
			DiscountType.FIXED,
			LocalDate.of(2025, 4, 1),
			LocalDate.of(2025, 4, 30)
		);
		PublishedCoupon published = PublishedCoupon.create(1L, coupon, LocalDate.of(2025, 4, 10));

		// when
		published.discount(BigDecimal.valueOf(10000), LocalDate.now());
		published.restore();

		// then
		assertThat(published.isUsed()).isFalse();
	}

	@Test
	@DisplayName("사용되지 않은 쿠폰을 restore() 하면 예외가 발생한다.")
	void restore_unusedCoupon_shouldThrow() {
		// given
		Coupon coupon = Coupon.createUnlimited(
			"미사용 쿠폰",
			BigDecimal.valueOf(5000),
			DiscountType.FIXED,
			LocalDate.of(2025, 4, 1),
			LocalDate.of(2025, 4, 30)
		);
		PublishedCoupon published = PublishedCoupon.create(1L, coupon, LocalDate.of(2025, 4, 10));

		// when & then
		assertThatThrownBy(published::restore)
			.isInstanceOf(CouponDoesNotUsedException.class)
			.hasMessage("비즈니스 정책을 위반한 요청입니다.");
	}
}