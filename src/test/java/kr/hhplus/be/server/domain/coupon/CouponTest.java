package kr.hhplus.be.server.domain.coupon;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import kr.hhplus.be.server.domain.coupon.discountpolicy.DiscountType;
import kr.hhplus.be.server.domain.coupon.exception.CouponExpiredException;
import kr.hhplus.be.server.domain.coupon.exception.CouponOutOfStockException;
import kr.hhplus.be.server.domain.coupon.issuePolicy.CouponIssuePolicyType;

class CouponTest {

	// 성공 테스트 먼저

	@Test
	@DisplayName("정상 입력값으로 무제한 쿠폰을 생성할 수 있다")
	void createUnlimitedCouponSuccessfully() {
		String name = "WELCOME";
		BigDecimal value = BigDecimal.valueOf(5000);
		DiscountType type = DiscountType.FIXED;
		LocalDate from = LocalDate.now();
		LocalDate to = from.plusDays(30);

		// when
		Coupon coupon = Coupon.createUnlimited(name, value, type, from, to);

		// then
		assertAll(
			() -> assertThat(coupon.getCouponName()).isEqualTo(name),
			() -> assertThat(coupon.getDiscountValue()).isEqualTo(value),
			() -> assertThat(coupon.getDiscountType()).isEqualTo(type),
			() -> assertThat(coupon.getIssuePolicyType()).isEqualTo(CouponIssuePolicyType.UNLIMITED),
			() -> assertThat(coupon.getRemainingCount()).isEqualTo(null),
			() -> assertThat(coupon.getValidFrom()).isEqualTo(from),
			() -> assertThat(coupon.getValidTo()).isEqualTo(to)
		);

	}

	@Test
	@DisplayName("정상 입력값으로 선착순 쿠폰을 생성할 수 있다")
	void createLimitedCouponSuccessfully() {
		String name = "WELCOME";
		BigDecimal value = BigDecimal.valueOf(5000);
		DiscountType type = DiscountType.FIXED;
		Long count = 100L;
		LocalDate from = LocalDate.now();
		LocalDate to = from.plusDays(30);

		// when
		Coupon coupon = Coupon.createLimited(name, value, type, count, from, to);

		// then
		assertAll(
			() -> assertThat(coupon.getCouponName()).isEqualTo(name),
			() -> assertThat(coupon.getDiscountValue()).isEqualTo(value),
			() -> assertThat(coupon.getDiscountType()).isEqualTo(type),
			() -> assertThat(coupon.getIssuePolicyType()).isEqualTo(CouponIssuePolicyType.LIMITED),
			() -> assertThat(coupon.getRemainingCount()).isEqualTo(count),
			() -> assertThat(coupon.getValidFrom()).isEqualTo(from),
			() -> assertThat(coupon.getValidTo()).isEqualTo(to)
		);
	}

	@ParameterizedTest
	@ValueSource(longs = {0L, -1L})
	@DisplayName("DiscountType이 FIXED일 때, 할인 금액은 0이하이면 IllegalArgumentException이 발생한다.")
	void createCouponWithInvalidFixedDiscountValue(long invalidDiscountValue) {
		// given
		String name = "WELCOME";
		BigDecimal value = BigDecimal.valueOf(invalidDiscountValue);
		DiscountType type = DiscountType.FIXED;
		LocalDate from = LocalDate.now();
		LocalDate to = from.plusDays(30);

		// when & then
		assertThatThrownBy(() -> Coupon.createUnlimited(name, value, type, from, to))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("고정 할인 금액은 0보다 커야 합니다.");
	}

	@ParameterizedTest
	@ValueSource(doubles = {0.0, -1.0, 101.0})
	@DisplayName("DiscountType이 PERCENTAGE일 때, 할인 비율은 0보다 크고 100보다 작아야 한다.")
	void createCouponWithInvalidPercentageDiscountValue(double invalidDiscountValue) {
		// given
		String name = "WELCOME";
		BigDecimal value = BigDecimal.valueOf(invalidDiscountValue);
		DiscountType type = DiscountType.PERCENTAGE;
		LocalDate from = LocalDate.now();
		LocalDate to = from.plusDays(30);

		// when & then
		assertThatThrownBy(() -> Coupon.createUnlimited(name, value, type, from, to))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("퍼센트 할인 비율은 0보다 크고 100보다 작아야 합니다.");
	}

	@Test
	@DisplayName("LIMITED 정책인 쿠폰은 issue() 시 수량이 1 감소된다.")
	void issue_withLimitedPolicy_shouldDecreaseRemainingCount() {
		// given
		Coupon coupon = Instancio.of(Coupon.class)
			.set(Select.field("issuePolicyType"), CouponIssuePolicyType.LIMITED)
			.set(Select.field("remainingCount"), 5L)
			.set(Select.field("discountType"), DiscountType.FIXED)
			.create();

		// when
		coupon.issue();

		// then
		assertThat(coupon.getRemainingCount()).isEqualTo(4L);
	}

	@Test
	@DisplayName("LIMITED 정책인데 남은 수량이 0이면 예외가 발생한다.")
	void issue_withLimitedPolicy_butNoRemaining_shouldFail() {
		// given
		Coupon coupon = Instancio.of(Coupon.class)
			.set(Select.field("issuePolicyType"), CouponIssuePolicyType.LIMITED)
			.set(Select.field("remainingCount"), 0L)
			.set(Select.field("discountType"), DiscountType.FIXED)
			.create();

		// when & then
		assertThatThrownBy(coupon::issue)
			.isInstanceOf(CouponOutOfStockException.class)
			.hasMessage("비즈니스 정책을 위반한 요청입니다.")
			.extracting("detail")
			.isEqualTo("쿠폰 ID : %d는 모두 소진되었습니다.".formatted(coupon.getId()));
	}

	@Test
	@DisplayName("Coupon에서 CouponSnapshot으로 변환 시 모든 값이 정확히 복사된다.")
	void toSnapshot_shouldReturnCorrectSnapshot() {
		// given
		Long id = 100L;
		String name = "여름 할인";
		DiscountType discountType = DiscountType.FIXED;
		BigDecimal discountValue = BigDecimal.valueOf(3000);
		LocalDate validFrom = LocalDate.of(2025, 6, 1);
		LocalDate validTo = LocalDate.of(2025, 6, 30);

		Coupon coupon = Instancio.of(Coupon.class)
			.set(Select.field("id"), id)
			.set(Select.field("couponName"), name)
			.set(Select.field("discountType"), discountType)
			.set(Select.field("discountValue"), discountValue)
			.set(Select.field("validFrom"), validFrom)
			.set(Select.field("validTo"), validTo)
			.set(Select.field("issuePolicyType"), CouponIssuePolicyType.LIMITED) // 무관하지만 생성 위해 필요
			.create();

		// when
		CouponSnapshot snapshot = coupon.toSnapShot();

		// then
		assertAll(
			() -> assertThat(snapshot.couponName()).isEqualTo(name),
			() -> assertThat(snapshot.discountType()).isEqualTo(discountType),
			() -> assertThat(snapshot.discountValue()).isEqualByComparingTo(discountValue),
			() -> assertThat(snapshot.validFrom()).isEqualTo(validFrom),
			() -> assertThat(snapshot.validTo()).isEqualTo(validTo),
			() -> assertThat(snapshot.originalCouponId()).isEqualTo(id)
		);
	}

	@Test
	@DisplayName("원장이 변해도 스냅샷 값은 동일하게 유지된다.")
	void snapshotShouldRemainUnchangedEvenIfOriginalChangesExternally() {
		// given
		Coupon original = Instancio.of(Coupon.class)
			.set(Select.field("id"), 1L)
			.set(Select.field("couponName"), "오리지널")
			.set(Select.field("discountValue"), BigDecimal.valueOf(1000))
			.set(Select.field("discountType"), DiscountType.FIXED)
			.set(Select.field("validFrom"), LocalDate.of(2025, 1, 1))
			.set(Select.field("validTo"), LocalDate.of(2025, 1, 31))
			.set(Select.field("issuePolicyType"), CouponIssuePolicyType.LIMITED)
			.create();

		CouponSnapshot snapshot = original.toSnapShot();

		// when: 원장 필드가 변경된 "새로운 쿠폰"을 만듬 (simulate 변경)
		Coupon changed = Instancio.of(Coupon.class)
			.set(Select.field("id"), 1L) // 같은 ID
			.set(Select.field("couponName"), "변경된 쿠폰")
			.set(Select.field("discountValue"), BigDecimal.valueOf(9999))
			.set(Select.field("discountType"), DiscountType.FIXED)
			.set(Select.field("validFrom"), LocalDate.of(2025, 3, 1))
			.set(Select.field("validTo"), LocalDate.of(2025, 3, 31))
			.set(Select.field("issuePolicyType"), CouponIssuePolicyType.LIMITED)
			.create();

		// then
		assertAll(
			() -> assertThat(snapshot.couponName()).isEqualTo("오리지널"),
			() -> assertThat(snapshot.discountValue()).isEqualByComparingTo("1000"),
			() -> assertThat(snapshot.discountType()).isEqualTo(DiscountType.FIXED),
			() -> assertThat(snapshot.validFrom()).isEqualTo(LocalDate.of(2025, 1, 1)),
			() -> assertThat(snapshot.validTo()).isEqualTo(LocalDate.of(2025, 1, 31)),
			() -> assertThat(snapshot.originalCouponId()).isEqualTo(1L)
		);
	}
}