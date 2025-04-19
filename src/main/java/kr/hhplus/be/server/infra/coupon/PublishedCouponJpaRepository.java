package kr.hhplus.be.server.infra.coupon;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.hhplus.be.server.domain.coupon.PublishedCoupon;

public interface PublishedCouponJpaRepository extends JpaRepository<PublishedCoupon, Long> {

	Optional<PublishedCoupon> findByUserIdAndCouponId(Long userId, Long couponId);

	boolean existsByUserIdAndCouponId(Long userId, Long couponId);
}
