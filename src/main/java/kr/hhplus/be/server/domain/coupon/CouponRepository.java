package kr.hhplus.be.server.domain.coupon;

public interface CouponRepository {

	Coupon save(Coupon coupon);

	Coupon findById(Long couponId);

	Coupon findByIdWithPessimistic(Long couponId);

	Coupon findByIdWithOptimistic(Long couponId);

	PublishedCoupon savePublishedCoupon(PublishedCoupon publishedCoupon);

	PublishedCoupon findPublishedCouponById(Long publishedCouponId);

	PublishedCoupon findPublishedCouponBy(Long userId, Long couponId);

	boolean existsPublishedCouponBy(Long userId, Long couponId);
}
