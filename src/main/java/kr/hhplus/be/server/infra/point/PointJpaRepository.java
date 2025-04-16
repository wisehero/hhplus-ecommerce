package kr.hhplus.be.server.infra.point;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.hhplus.be.server.domain.point.Point;

public interface PointJpaRepository extends JpaRepository<Point, Long> {

	Optional<Point> findByUserId(Long userId);

}
