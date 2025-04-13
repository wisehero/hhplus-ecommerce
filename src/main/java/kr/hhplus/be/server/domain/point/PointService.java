package kr.hhplus.be.server.domain.point;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.domain.point.dto.PointChargeCommand;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointService {

	private final PointRepository pointRepository;

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
		PointHistory pointHistory = PointHistory.create(userPoint, command.chargeAmount(), TransactionType.CHARGE);

		return pointRepository.saveWithHistory(chargedUserPoint, pointHistory);
	}

	@Transactional
	public Point useUserPoint(Long userId, BigDecimal useAmount) {
		Point userPoint = pointRepository.findByUserId(userId);

		Point usedUserPoint = userPoint.use(useAmount);

		PointHistory pointHistory = PointHistory.create(userPoint, useAmount, TransactionType.USE);
		return pointRepository.saveWithHistory(usedUserPoint, pointHistory);
	}
}
