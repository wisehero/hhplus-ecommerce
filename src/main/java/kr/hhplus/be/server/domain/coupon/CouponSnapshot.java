package kr.hhplus.be.server.domain.coupon;

import java.math.BigDecimal;
import java.time.LocalDate;

import kr.hhplus.be.server.domain.coupon.discountpolicy.DiscountType;

public record CouponSnapshot(
	String couponName,
	DiscountType discountType,
	BigDecimal discountValue,
	LocalDate validFrom,
	LocalDate validTo,
	Long originalCouponId          // 추적용
) {}
