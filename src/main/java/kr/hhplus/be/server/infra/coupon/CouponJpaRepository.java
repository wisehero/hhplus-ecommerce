package kr.hhplus.be.server.infra.coupon;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.domain.coupon.Coupon;

public interface CouponJpaRepository extends JpaRepository<Coupon, Long> {

	@Lock(value = LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT c FROM Coupon c WHERE c.id = :couponId")
	Optional<Coupon> findByIdWithPessimistic(@Param("couponId") Long couponId);
}
