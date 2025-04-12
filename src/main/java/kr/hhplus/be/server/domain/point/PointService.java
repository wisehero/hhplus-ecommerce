package kr.hhplus.be.server.domain.point;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.domain.point.dto.PointChargeCommand;
import kr.hhplus.be.server.domain.point.pointhistory.PointHistory;
import kr.hhplus.be.server.domain.point.pointhistory.PointHistoryRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointService {

	private final PointRepository pointRepository;
	private final PointHistoryRepository pointHistoryRepository;

	public Point getPointOfUser(Long userId) {
		Point userPoint = pointRepository.findByUserId(userId);

		if (userPoint == null) {
			userPoint = Point.createZeroUserPoint(userId);
			return pointRepository.save(userPoint);
		}

		return userPoint;
	}

	@Transactional
	public Point chargeUserPoint(PointChargeCommand command) {
		Point userPoint = pointRepository.findByUserId(command.userId());

		Point chargedUserPoint = userPoint.charge(command.chargeAmount());

		PointHistory pointHistory = PointHistory.createPointHistory(userPoint, command.chargeAmount(),
			TransactionType.CHARGE);
		pointHistoryRepository.save(pointHistory);

		return pointRepository.save(chargedUserPoint);
	}

	@Transactional
	public Point useUserPoint(Long userId, BigDecimal useAmount) {
		Point userPoint = pointRepository.findByUserId(userId);

		userPoint.use(useAmount);

		pointHistoryRepository.save(
			PointHistory.createPointHistory(userPoint, useAmount, TransactionType.USE));

		return pointRepository.save(userPoint);
	}
}
