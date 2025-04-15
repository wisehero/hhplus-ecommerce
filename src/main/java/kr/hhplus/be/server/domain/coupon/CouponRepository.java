package kr.hhplus.be.server.domain.coupon;

public interface CouponRepository {

	Coupon save(Coupon coupon);

	Coupon findById(Long couponId);

	PublishedCoupon savePublishedCoupon(PublishedCoupon publishedCoupon);

	PublishedCoupon findPublishedCouponBy(Long publishedCouponId);

	boolean existsPublishedCouponBy(Long userId, Long couponId);
}
