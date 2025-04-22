package kr.hhplus.be.server.infra.coupon;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityNotFoundException;
import kr.hhplus.be.server.domain.coupon.Coupon;
import kr.hhplus.be.server.domain.coupon.CouponRepository;
import kr.hhplus.be.server.domain.coupon.PublishedCoupon;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CouponRepositoryImpl implements CouponRepository {

	private final CouponJpaRepository couponJpaRepository;
	private final PublishedCouponJpaRepository publishedCouponJpaRepository;

	@Override
	public Coupon save(Coupon coupon) {
		return couponJpaRepository.save(coupon);
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
}
