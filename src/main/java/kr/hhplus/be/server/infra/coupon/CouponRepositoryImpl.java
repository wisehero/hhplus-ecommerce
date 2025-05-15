package kr.hhplus.be.server.infra.coupon;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityNotFoundException;
import kr.hhplus.be.server.domain.coupon.Coupon;
import kr.hhplus.be.server.domain.coupon.CouponRepository;
import kr.hhplus.be.server.domain.coupon.PublishedCoupon;
import kr.hhplus.be.server.domain.coupon.exception.CouponOutOfStockException;
import kr.hhplus.be.server.infra.coupon.redis.CouponRedisRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CouponRepositoryImpl implements CouponRepository {

	private final CouponJpaRepository couponJpaRepository;
	private final PublishedCouponJpaRepository publishedCouponJpaRepository;
	private final CouponRedisRepository couponRedisRepository;

	@Override
	public Coupon save(Coupon coupon) {
		return couponJpaRepository.save(coupon);
	}

	@Override
	public void saveAll(List<PublishedCoupon> publishedCoupons) {
		if (publishedCoupons.isEmpty())
			return;

		Long couponId = publishedCoupons.get(0).getCouponId();
		Coupon coupon = couponJpaRepository.findById(couponId).orElseThrow(
			() -> new EntityNotFoundException("쿠폰을 찾을 수 없습니다.")
		);

		coupon.decreaseStock(publishedCoupons.size());

		couponJpaRepository.save(coupon);
		publishedCouponJpaRepository.saveAll(publishedCoupons);
	}

	@Override
	public Coupon findById(Long couponId) {
		return couponJpaRepository.findById(couponId).orElseThrow(
			() -> new EntityNotFoundException("쿠폰을 찾을 수 없습니다.")
		);
	}

	@Override
	public Coupon findByIdWithPessimistic(Long couponId) {
		return couponJpaRepository.findByIdWithPessimistic(couponId).orElseThrow(
			() -> new EntityNotFoundException("쿠폰을 찾을 수 없습니다.")
		);
	}

	@Override
	public Coupon findByIdWithOptimistic(Long couponId) {
		return couponJpaRepository.findByIdWithOptimistic(couponId).orElseThrow(
			() -> new EntityNotFoundException("쿠폰을 찾을 수 없습니다.")
		);
	}

	@Override
	public PublishedCoupon savePublishedCoupon(PublishedCoupon publishedCoupon) {
		return publishedCouponJpaRepository.save(publishedCoupon);
	}

	@Override
	public PublishedCoupon findPublishedCouponById(Long publishedCouponId) {
		return publishedCouponJpaRepository.findById(publishedCouponId).orElseThrow(
			() -> new EntityNotFoundException("발행된 쿠폰을 찾을 수 없습니다.")
		);
	}

	@Override
	public PublishedCoupon findPublishedCouponBy(Long userId, Long couponId) {
		return publishedCouponJpaRepository.findByUserIdAndCouponId(userId, couponId).orElseThrow(
			() -> new EntityNotFoundException("발행된 쿠폰을 찾을 수 없습니다.")
		);
	}

	@Override
	public boolean existsPublishedCouponBy(Long userId, Long couponId) {
		return publishedCouponJpaRepository.existsByUserIdAndCouponId(userId, couponId);
	}

	@Override
	public boolean addIfAbsent(Long couponId, Long userId) {
		return couponRedisRepository.addIfAbsent(couponId, userId);
	}

	@Override
	public Set<String> getNextBatchFromQueue(Long couponId, int batchSize, int availableStock) {
		return couponRedisRepository.getNextBatchFromQueue(couponId, batchSize, availableStock);
	}

	@Override
	public void removeFromQueue(Long couponId, Set<String> userIds) {
		couponRedisRepository.removeFromQueue(couponId, userIds);
	}

	@Override
	public Set<Long> getAllCouponIds() {
		return couponRedisRepository.getAllCouponIds();
	}
}
