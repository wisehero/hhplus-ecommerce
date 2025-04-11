package kr.hhplus.be.server.domain.coupon.discountpolicy;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DiscountTypeTest {

	@Test
	@DisplayName("모든 DiscountType은 toPolicy 메서드를 통해 정책을 생성할 수 있어야 한다.")
	void allDiscountTypesSupportToPolicy() {
		Arrays.stream(DiscountType.values())
			.forEach(discountType -> {
				BigDecimal value = BigDecimal.valueOf(10);
				DiscountPolicy policy = discountType.toPolicy(value);

				assertThat(policy).isNotNull();
				assertThat(policy).isInstanceOf(DiscountPolicy.class);
			});
	}

	@Test
	@DisplayName("FIXED 타입은 FixedDiscountPolicy를 생성하고 정액 할인을 적용한다.")
	void fixedTypeCreatesFixedDiscountPolicy() {
		// given
		DiscountPolicy policy = DiscountType.FIXED.toPolicy(BigDecimal.valueOf(1000));

		// when
		BigDecimal result = policy.apply(BigDecimal.valueOf(5000));

		// then
		assertAll(
			() -> assertThat(policy).isInstanceOf(FixedDiscountPolicy.class),
			() -> assertThat(result).isEqualTo(BigDecimal.valueOf(4000))
		);
	}

	@Test
	@DisplayName("PERCENTAGE 타입은 PercentageDiscountPolicy를 생성하고 정률 할인을 적용한다.")
	void percentageTypeCreatesPercentageDiscountPolicy() {
		// given
		DiscountPolicy policy = DiscountType.PERCENTAGE.toPolicy(BigDecimal.valueOf(10));

		// when
		BigDecimal result = policy.apply(BigDecimal.valueOf(10000));

		// then
		assertAll(
			() -> assertThat(policy).isInstanceOf(PercentageDiscountPolicy.class),
			() -> assertThat(result).isEqualTo(BigDecimal.valueOf(9000))
		);
	}
}