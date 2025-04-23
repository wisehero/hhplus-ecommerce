package kr.hhplus.be.server.infra.point;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.domain.point.Point;

public interface PointJpaRepository extends JpaRepository<Point, Long> {

	Optional<Point> findByUserId(Long userId);

	@Lock(LockModeType.OPTIMISTIC)
	@Query("SELECT p FROM Point p WHERE p.userId = :userId")
	Optional<Point> findByUserIdOptimistic(Long userId);

}
