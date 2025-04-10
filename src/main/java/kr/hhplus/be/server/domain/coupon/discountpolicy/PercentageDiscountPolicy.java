package kr.hhplus.be.server.domain.coupon.discountpolicy;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PercentageDiscountPolicy implements DiscountPolicy {
	private final BigDecimal rate;

	public PercentageDiscountPolicy(BigDecimal rate) {
		if (rate.compareTo(BigDecimal.ZERO) <= 0 || rate.compareTo(BigDecimal.valueOf(100)) > 0) {
			// 할인율이 0보다 작거나 100보다 큰 경우 예외 처리
			throw new IllegalArgumentException("정률 할인은 0보다 크고 100 이하여야 합니다.");
		}
		this.rate = rate;
	}

	@Override
	public BigDecimal apply(BigDecimal originalPrice) {
		BigDecimal discount = originalPrice.multiply(rate)
			.divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP); // ← 반올림 명시

		return originalPrice.subtract(discount);
	}
}
