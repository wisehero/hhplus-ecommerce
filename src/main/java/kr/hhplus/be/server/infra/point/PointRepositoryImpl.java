package kr.hhplus.be.server.infra.point;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.hhplus.be.server.domain.point.Point;
import kr.hhplus.be.server.domain.point.PointHistory;
import kr.hhplus.be.server.domain.point.PointRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PointRepositoryImpl implements PointRepository {

	private final PointJpaRepository pointJpaRepository;
	private final PointHistoryJpaRepository pointHistoryJpaRepository;

	@Override
	public Point findByUserId(Long userId) {
		return pointJpaRepository.findByUserId(userId)
			.orElseGet(
				() -> {
					Point point = Point.createZeroUserPoint(userId);
					return pointJpaRepository.save(point);
				}
			);
	}

	@Override
	public Point findByUserIdWithOptimistic(Long userId) {
		return pointJpaRepository.findByUserIdOptimistic(userId)
			.orElseGet(
				() -> {
					Point point = Point.createZeroUserPoint(userId);
					return pointJpaRepository.save(point);
				}
			);
	}

	@Override
	public Point save(Point point) {
		return pointJpaRepository.save(point);
	}

	@Override
	public Point saveWithHistory(Point point, PointHistory pointHistory) {
		Point savedPoint = pointJpaRepository.save(point);
		pointHistoryJpaRepository.save(pointHistory);
		return savedPoint;
	}

	@Override
	public List<PointHistory> getPointHistoryByPointId(Long pointId) {
		return pointHistoryJpaRepository.findByPointId(pointId);
	}
}
