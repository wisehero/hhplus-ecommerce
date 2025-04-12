package kr.hhplus.be.server.domain.coupon;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import kr.hhplus.be.server.domain.coupon.discountpolicy.DiscountType;
import kr.hhplus.be.server.domain.coupon.exception.CouponAlreadyUsedException;
import kr.hhplus.be.server.domain.coupon.exception.CouponDoesNotBelongToUserException;
import kr.hhplus.be.server.domain.coupon.exception.CouponExpiredException;

class UserCouponTest {

	@Test
	@DisplayName("사용자에게 발급된 쿠폰 객체 생성시 사용되지 않은 상태다.")
	void createsUnusedCoupon() {
		// given
		Coupon coupon = Coupon.createUnlimitedCoupon(
			"테스트 쿠폰",
			DiscountType.FIXED,
			BigDecimal.valueOf(3000),
			LocalDate.now().minusDays(1),
			LocalDate.now().plusDays(5)
		);

		UserCoupon userCoupon = UserCoupon.create(1L, coupon);

		assertAll(
			() -> assertThat(userCoupon.getUserId()).isEqualTo(1L),
			() -> assertThat(userCoupon.getCouponId()).isEqualTo(coupon.getId()),
			() -> assertThat(userCoupon.isUsed()).isFalse()
		);
	}

	@Test
	@DisplayName("UserCoupon은 사용 후 다시 사용할 경우 CouponAlreadyUsedException을 던진다.")
	void markUsedTwiceThrowsException() {
		// given
		Coupon coupon = Coupon.createUnlimitedCoupon(
			"테스트 쿠폰",
			DiscountType.FIXED,
			BigDecimal.valueOf(3000),
			LocalDate.now().minusDays(1),
			LocalDate.now().plusDays(5));
		UserCoupon userCoupon = UserCoupon.create(1L, coupon);
		userCoupon.markUsed();

		assertThatThrownBy(userCoupon::markUsed)
			.isInstanceOf(CouponAlreadyUsedException.class)
			.hasMessage("비즈니스 정책을 위반한 요청입니다.")
			.extracting("detail")
			.isEqualTo("쿠폰이 이미 사용되었습니다.");
	}

	@Test
	@DisplayName("UserCoupon의 만료 여부는 expiredAt을 기준으로 현재 날짜와 비교한다")
	void userCouponIsExpiredBoundaryCases() {
		LocalDate expiredAt = LocalDate.of(2025, 4, 10);

		UserCoupon userCoupon = UserCoupon.createUserCoupon(
			1L,
			1L,
			"테스트 쿠폰",
			LocalDate.of(2025, 4, 8),
			expiredAt
		);

		assertAll(
			() -> assertThatCode(() -> userCoupon.validateUsable(userCoupon.getCouponId(), LocalDate.of(2025, 4, 8)))
				.doesNotThrowAnyException(),

			() -> assertThatCode(() -> userCoupon.validateUsable(userCoupon.getUserId(), LocalDate.of(2025, 4, 10)))
				.doesNotThrowAnyException(),

			() -> assertThatThrownBy(() -> userCoupon.validateUsable(userCoupon.getUserId(), LocalDate.of(2025, 4, 11)))
				.isInstanceOf(CouponExpiredException.class)
				.hasMessage("비즈니스 정책을 위반한 요청입니다.")
				.extracting("detail")
				.isEqualTo("쿠폰이 만료되었습니다. 유효 기간은 %s까지 입니다.".formatted(expiredAt)));
	}

	@DisplayName("다른 사용자의 쿠폰일 경우 예외를 발생시킨다")
	@Test
	void userCouponWithWrongUserIdFails() {
		UserCoupon userCoupon = UserCoupon.createUserCoupon(
			1L,
			1L,
			"쿠폰",
			LocalDate.now().minusDays(1),
			LocalDate.now().plusDays(1)
		);

		assertThatThrownBy(() -> userCoupon.validateUsable(2L, LocalDate.now()))
			.isInstanceOf(CouponDoesNotBelongToUserException.class)
			.hasMessage("비즈니스 정책을 위반한 요청입니다.")
			.extracting("detail")
			.isEqualTo("사용자가 보유한 쿠폰이 아닙니다.");
	}

}