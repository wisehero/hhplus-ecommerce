package kr.hhplus.be.server.domain.coupon.discountpolicy;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class FixedDiscountPolicyTest {
	@Test
	@DisplayName("정액 할인을 정상적으로 적용한다")
	void applyFixedDiscountSuccessfully() {
		FixedDiscountPolicy policy = new FixedDiscountPolicy(BigDecimal.valueOf(1000));

		BigDecimal result = policy.apply(BigDecimal.valueOf(5000));

		assertThat(result).isEqualByComparingTo("4000");
	}

	@Test
	@DisplayName("할인 금액이 주문 금액보다 클 경우 0원이 된다")
	void discountExceedingPriceReturnsZero() {
		FixedDiscountPolicy policy = new FixedDiscountPolicy(BigDecimal.valueOf(10000));

		BigDecimal result = policy.apply(BigDecimal.valueOf(5000));

		assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
	}
}