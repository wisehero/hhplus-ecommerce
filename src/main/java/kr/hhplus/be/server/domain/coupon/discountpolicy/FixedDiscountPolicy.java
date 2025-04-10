package kr.hhplus.be.server.domain.coupon.discountpolicy;

import java.math.BigDecimal;

public class FixedDiscountPolicy implements DiscountPolicy {

	private final BigDecimal discountAmount;

	public FixedDiscountPolicy(BigDecimal discountAmount) {
		if (discountAmount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("고정 할인 금액은 0보다 커야 합니다.");
		}
		this.discountAmount = discountAmount;
	}

	@Override
	public BigDecimal apply(BigDecimal originalPrice) {
		BigDecimal discounted = originalPrice.subtract(discountAmount);
		return discounted.max(BigDecimal.ZERO);
	}
}
