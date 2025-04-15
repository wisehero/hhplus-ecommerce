package kr.hhplus.be.server.domain.coupon.discountpolicy;

import java.math.BigDecimal;

public enum DiscountType {
	FIXED {
		@Override
		public DiscountPolicy toPolicy(BigDecimal value) {
			return new FixedDiscountPolicy(value);
		}
	},
	PERCENTAGE {
		@Override
		public DiscountPolicy toPolicy(BigDecimal value) {
			return new PercentageDiscountPolicy(value);
		}
	};

	public abstract DiscountPolicy toPolicy(BigDecimal value);
}
