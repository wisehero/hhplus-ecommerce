package kr.hhplus.be.server.domain.point;

public interface PointRepository {

	Point findByUserId(Long userId);

	Point save(Point point);

	Point saveWithHistory(Point point, PointHistory pointHistory);

}
