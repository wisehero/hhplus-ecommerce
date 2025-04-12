package kr.hhplus.be.server.domain.coupon.discountpolicy;

import java.math.BigDecimal;

public interface DiscountPolicy {
	BigDecimal apply(BigDecimal originalPrice);
}
