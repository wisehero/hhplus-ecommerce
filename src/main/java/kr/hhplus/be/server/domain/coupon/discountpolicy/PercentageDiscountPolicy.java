package kr.hhplus.be.server.domain.coupon.discountpolicy;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PercentageDiscountPolicy implements DiscountPolicy {
	private final BigDecimal rate;

	public PercentageDiscountPolicy(BigDecimal rate) {
		this.rate = rate;
	}

	@Override
	public BigDecimal apply(BigDecimal originalPrice) {
		BigDecimal discount = originalPrice.multiply(rate)
			.divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP); // ← 반올림 명시

		return originalPrice.subtract(discount);
	}
}
