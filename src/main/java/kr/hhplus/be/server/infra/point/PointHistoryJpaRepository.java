package kr.hhplus.be.server.infra.point;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.hhplus.be.server.domain.point.PointHistory;

public interface PointHistoryJpaRepository extends JpaRepository<PointHistory, Long> {

	List<PointHistory> findByPointId(Long pointId);
}
