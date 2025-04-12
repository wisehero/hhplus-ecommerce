package kr.hhplus.be.server.domain.coupon;

public interface UserCouponRepository {

	UserCoupon findById(Long id);

	UserCoupon save(UserCoupon userCoupon);

	boolean existsByUserIdAndCouponId(Long userId, Long couponId);
}
