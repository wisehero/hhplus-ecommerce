package kr.hhplus.be.server.domain.coupon;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import kr.hhplus.be.server.domain.coupon.discountpolicy.DiscountType;
import kr.hhplus.be.server.domain.coupon.exception.CouponExpiredException;
import kr.hhplus.be.server.domain.coupon.exception.CouponOutOfStockException;

class CouponTest {

	// 성공 테스트 먼저

	@Test
	@DisplayName("정상 입력값으로 무제한 쿠폰을 생성할 수 있다")
	void createUnlimitedCouponSuccessfully() {
		// when
		Coupon coupon = Coupon.createUnlimitedCoupon(
			"무제한 쿠폰",
			DiscountType.FIXED,
			BigDecimal.valueOf(2000),
			LocalDate.now(),
			LocalDate.now().plusDays(10)
		);

		// then
		assertAll(
			() -> assertThat(coupon.getCouponName()).isEqualTo("무제한 쿠폰"),
			() -> assertThat(coupon.getDiscountType()).isEqualTo(DiscountType.FIXED),
			() -> assertThat(coupon.getDiscountValue()).isEqualTo("2000"),
			() -> assertThat(coupon.getIssuePolicyType().name()).isEqualTo("UNLIMITED"),
			() -> assertThat(coupon.getRemainingCount()).isNull()
		);
	}

	@Test
	@DisplayName("정상 입력값으로 선착순 쿠폰을 생성할 수 있다")
	void createLimitedCouponSuccessfully() {
		// when
		Coupon coupon = Coupon.createLimitedCoupon(
			"선착순 쿠폰",
			DiscountType.PERCENTAGE,
			BigDecimal.valueOf(15),
			100L,
			LocalDate.now(),
			LocalDate.now().plusDays(7)
		);

		// then
		assertAll(
			() -> assertThat(coupon.getCouponName()).isEqualTo("선착순 쿠폰"),
			() -> assertThat(coupon.getDiscountType()).isEqualTo(DiscountType.PERCENTAGE),
			() -> assertThat(coupon.getDiscountValue()).isEqualTo("15"),
			() -> assertThat(coupon.getIssuePolicyType().name()).isEqualTo("LIMITED"),
			() -> assertThat(coupon.getRemainingCount()).isEqualTo(100L)
		);
	}

	@Test
	@DisplayName("정률 쿠폰은 금액의 일정 비율만큼 할인된다.")
	void percentageDiscountIsAppliedCorrectly() {
		// given
		Coupon coupon = Coupon.createUnlimitedCoupon(
			"10% 할인 쿠폰",
			DiscountType.PERCENTAGE,
			BigDecimal.valueOf(10),
			LocalDate.now().minusDays(1),
			LocalDate.now().plusDays(1)
		);

		BigDecimal originalPrice = BigDecimal.valueOf(10000);

		// when
		BigDecimal discounted = coupon.applyDiscount(originalPrice);

		// then
		assertThat(discounted).isEqualTo("9000");
	}

	@Test
	@DisplayName("정액 할인 쿠폰이 원가에서 고정 금액을 차감한다.")
	void fixedDiscountIsAppliedCorrectly() {
		// given
		Coupon coupon = Coupon.createUnlimitedCoupon(
			"3000원 할인 쿠폰",
			DiscountType.FIXED,
			BigDecimal.valueOf(3000),
			LocalDate.now().minusDays(1),
			LocalDate.now().plusDays(1)
		);

		BigDecimal originalPrice = BigDecimal.valueOf(10000);

		// when
		BigDecimal discounted = coupon.applyDiscount(originalPrice);

		// then
		assertThat(discounted).isEqualTo("7000");
	}

	@Test
	@DisplayName("선착순 쿠폰 발급 시 수량이 줄어든다.")
	void limitedCouponDecreasesStockWhenIssued() {
		// given
		Coupon coupon = Coupon.createLimitedCoupon(
			"선착순 할인 쿠폰",
			DiscountType.FIXED,
			BigDecimal.valueOf(1000),
			5L,
			LocalDate.now().minusDays(1),
			LocalDate.now().plusDays(1)
		);

		// when
		coupon.issue();

		// then
		assertThat(coupon.getRemainingCount()).isEqualTo(4L);
	}

	@Test
	@DisplayName("유효 기간 내에서는 쿠폰이 유효하다.")
	void couponIsValidWithinPeriod() {
		// given
		Coupon coupon = Coupon.createUnlimitedCoupon(
			"기간 한정 쿠폰",
			DiscountType.FIXED,
			BigDecimal.valueOf(1000),
			LocalDate.now().minusDays(2),
			LocalDate.now().plusDays(2)
		);

		// then
		assertThatCode(() -> coupon.validateIssueAvailable(LocalDate.now()))
			.doesNotThrowAnyException();
	}

	@Test
	@DisplayName("무제한 쿠폰은 발급 시 예외 없이 성공한다")
	void unlimitedCouponIssuesSuccessfully() {
		// given
		Coupon coupon = Coupon.createUnlimitedCoupon(
			"무제한 쿠폰",
			DiscountType.FIXED,
			BigDecimal.valueOf(500),
			LocalDate.now().minusDays(1),
			LocalDate.now().plusDays(1)
		);

		// expected
		assertThatCode(coupon::issue).doesNotThrowAnyException();
	}

	// 실패 테스트
	@Test
	@DisplayName("할인 값이 0 이하이면 정액 무제한/제한 쿠폰 모두 생성에 실패한다.")
	void fixedDiscountValueZeroOrNegativeShouldFail() {
		assertAll(
			() -> assertThatThrownBy(() ->
				Coupon.createUnlimitedCoupon(
					"0원 할인 쿠폰",
					DiscountType.FIXED,
					BigDecimal.ZERO,
					LocalDate.now(),
					LocalDate.now().plusDays(1)
				)
			).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("할인 값은 0보다 커야 합니다."),

			() -> assertThatThrownBy(() ->
				Coupon.createUnlimitedCoupon(
					"마이너스 할인 쿠폰",
					DiscountType.FIXED,
					BigDecimal.valueOf(-500),
					LocalDate.now(),
					LocalDate.now().plusDays(1)
				)
			).isInstanceOf(IllegalArgumentException.class)
				.hasMessage("할인 값은 0보다 커야 합니다.")
		);
	}

	@Test
	@DisplayName("할인 값이 0 이하이면 정률 무제한/제한 쿠폰 모두 생성에 실패한다.")
	void percentageDiscountValueZeroOrNegativeShouldFail() {
		assertAll(
			() -> assertThatThrownBy(() ->
				Coupon.createUnlimitedCoupon(
					"0% 할인 쿠폰",
					DiscountType.PERCENTAGE,
					BigDecimal.ZERO,
					LocalDate.now(),
					LocalDate.now().plusDays(1)
				)
			).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("할인 값은 0보다 커야 합니다."),

			() -> assertThatThrownBy(() ->
				Coupon.createUnlimitedCoupon(
					"마이너스 % 할인 쿠폰",
					DiscountType.PERCENTAGE,
					BigDecimal.valueOf(-10),
					LocalDate.now(),
					LocalDate.now().plusDays(1)
				)
			).isInstanceOf(IllegalArgumentException.class)
				.hasMessage("할인 값은 0보다 커야 합니다.")
		);
	}

	@Test
	@DisplayName("정률 할인 값이 100% 초과일 경우 예외가 발생한다")
	void percentageDiscountOver100Fails() {
		assertThatThrownBy(() ->
			Coupon.createUnlimitedCoupon(
				"150% 할인 쿠폰",
				DiscountType.PERCENTAGE,
				BigDecimal.valueOf(150),
				LocalDate.now(),
				LocalDate.now().plusDays(1)
			)
		).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("정률 할인은 100%를 넘을 수 없습니다.");
	}

	@Test
	@DisplayName("현재 날짜가 유효 기간을 벗어나면 쿠폰은 무효하다")
	void couponInvalidOutsideValidityPeriod() {
		// given
		LocalDate startDate = LocalDate.now().minusDays(10);
		LocalDate endDate = LocalDate.now().minusDays(1);
		Coupon coupon = Coupon.createUnlimitedCoupon(
			"만료 쿠폰",
			DiscountType.FIXED,
			BigDecimal.valueOf(1000),
			startDate,
			endDate
		);

		// expected
		assertThatThrownBy(() -> coupon.validateIssueAvailable(LocalDate.now()))
			.isInstanceOf(CouponExpiredException.class)
			.hasMessage("비즈니스 정책을 위반한 요청입니다.")
			.extracting("detail")
			.isEqualTo("쿠폰이 만료되었습니다. 유효 기간은 %s까지 입니다.".formatted(endDate)); // 메시지 정의에 따라 조절
	}

	@Test
	@DisplayName("선착순 쿠폰의 수량이 0이면 발급 시 예외가 발생한다")
	void limitedCouponOutOfStockShouldThrow() {
		// given
		Long remainingCount = 0L;
		Coupon coupon = Coupon.createLimitedCoupon(
			"재고 없음 쿠폰",
			DiscountType.FIXED,
			BigDecimal.valueOf(1000),
			remainingCount,
			LocalDate.now().minusDays(1),
			LocalDate.now().plusDays(1)
		);

		// expected
		assertThatThrownBy(coupon::issue)
			.isInstanceOf(CouponOutOfStockException.class)
			.hasMessage("비즈니스 정책을 위반한 요청입니다.")
			.extracting("detail")
			.isEqualTo("쿠폰 ID : %d는 모두 소진되었습니다.".formatted(coupon.getId()));
	}
}