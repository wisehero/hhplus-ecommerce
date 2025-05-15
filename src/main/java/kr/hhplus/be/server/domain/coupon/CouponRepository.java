package kr.hhplus.be.server.domain.coupon;

import java.util.List;
import java.util.Set;

public interface CouponRepository {

	Coupon save(Coupon coupon);

	void saveAll(List<PublishedCoupon> publishedCoupons);

	Coupon findById(Long couponId);

	Coupon findByIdWithPessimistic(Long couponId);

	Coupon findByIdWithOptimistic(Long couponId);

	PublishedCoupon savePublishedCoupon(PublishedCoupon publishedCoupon);

	PublishedCoupon findPublishedCouponById(Long publishedCouponId);

	PublishedCoupon findPublishedCouponBy(Long userId, Long couponId);

	boolean existsPublishedCouponBy(Long userId, Long couponId);

	boolean addIfAbsent(Long couponId, Long userId);

	Set<String> getNextBatchFromQueue(Long couponId, int batchSize, int availableStock);

	void removeFromQueue(Long couponId, Set<String> userIds);

	Set<Long> getAllCouponIds();
}
