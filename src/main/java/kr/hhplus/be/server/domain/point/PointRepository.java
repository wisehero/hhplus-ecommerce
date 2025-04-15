package kr.hhplus.be.server.domain.point;

import java.util.List;

public interface PointRepository {

	Point findByUserId(Long userId);

	Point save(Point point);

	Point saveWithHistory(Point point, PointHistory pointHistory);

	List<PointHistory> getPointHistoryByPointId(Long pointId);
}
