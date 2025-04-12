package kr.hhplus.be.server.domain.coupon;

public interface CouponRepository {

	Coupon findById(Long couponId);

	Coupon save(Coupon coupon);

}
