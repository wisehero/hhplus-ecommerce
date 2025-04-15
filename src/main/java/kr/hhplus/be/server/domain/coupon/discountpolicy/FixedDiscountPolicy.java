package kr.hhplus.be.server.domain.coupon.discountpolicy;

import java.math.BigDecimal;

public class FixedDiscountPolicy implements DiscountPolicy {

	private final BigDecimal discountAmount;

	public FixedDiscountPolicy(BigDecimal discountAmount) {
		this.discountAmount = discountAmount;
	}

	@Override
	public BigDecimal apply(BigDecimal originalPrice) {
		BigDecimal discounted = originalPrice.subtract(discountAmount);
		return discounted.max(BigDecimal.ZERO);
	}
}
