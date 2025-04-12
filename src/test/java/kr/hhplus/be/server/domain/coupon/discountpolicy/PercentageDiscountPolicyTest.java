package kr.hhplus.be.server.domain.coupon.discountpolicy;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PercentageDiscountPolicyTest {

	@Test
	@DisplayName("정률 할인을 정상적으로 적용한다.")
	void applyPercentageDiscountSuccessfully() {
		// given
		PercentageDiscountPolicy percentageDiscountPolicy = new PercentageDiscountPolicy(BigDecimal.valueOf(10));
		BigDecimal originalPrice = BigDecimal.valueOf(10000);

		// when
		BigDecimal discountedPrice = percentageDiscountPolicy.apply(originalPrice);

		// then
		assertThat(discountedPrice).isEqualTo(BigDecimal.valueOf(9000));
	}

	@Test
	@DisplayName("정률 할인은 반올림하여 계산한다 (25.5%)")
	void applyPercentageDiscountRoundsHalfUp() {
		// given
		PercentageDiscountPolicy policy = new PercentageDiscountPolicy(BigDecimal.valueOf(25.5));
		BigDecimal originalPrice = BigDecimal.valueOf(10000);

		// when
		BigDecimal result = policy.apply(originalPrice);

		assertThat(result).isEqualTo(BigDecimal.valueOf(7450)); // 2550 할인 → 반올림
	}

	@ParameterizedTest
	@ValueSource(strings = {"0", "-10", "150"})
	@DisplayName("할인율이 0 이하 또는 100 초과일 경우 예외가 발생한다")
	void invalidPercentageRateThrowsException(String rate) {
		assertThatThrownBy(() -> new PercentageDiscountPolicy(new BigDecimal(rate)))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("정률 할인은 0보다 크고 100 이하여야 합니다.");
	}
}