package kr.hhplus.be.server.domain.point;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.domain.point.dto.PointChargeCommand;
import kr.hhplus.be.server.domain.point.dto.PointUseCommand;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointService {

	private final PointRepository pointRepository;

	public Point getPointOfUser(Long userId) {
		if (userId == null) {
			throw new IllegalArgumentException("userId는 null일 수 없습니다.");
		}

		return pointRepository.findByUserId(userId);
	}

	@Transactional
	public Point chargeUserPoint(PointChargeCommand command) {
		if (command.userId() == null) {
			throw new IllegalArgumentException("userId는 null일 수 없습니다.");
		}

		Point userPoint = pointRepository.findByUserId(command.userId());

		Point chargedUserPoint = userPoint.charge(command.chargeAmount());

		PointHistory pointHistory = PointHistory.create(userPoint, command.chargeAmount(), TransactionType.CHARGE);
		return pointRepository.saveWithHistory(chargedUserPoint, pointHistory);
	}

	@Transactional
	public Point useUserPoint(PointUseCommand command) {
		if (command.userId() == null) {
			throw new IllegalArgumentException("userId는 null일 수 없습니다.");
		}
		Point userPoint = pointRepository.findByUserId(command.userId());

		Point usedUserPoint = userPoint.use(command.useAmount());

		PointHistory pointHistory = PointHistory.create(userPoint, command.useAmount(), TransactionType.USE);
		return pointRepository.saveWithHistory(usedUserPoint, pointHistory);
	}
}
