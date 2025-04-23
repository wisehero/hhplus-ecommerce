package kr.hhplus.be.server.domain.point;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.domain.point.dto.PointChargeCommand;
import kr.hhplus.be.server.domain.point.dto.PointUseCommand;
import kr.hhplus.be.server.domain.point.exception.PointChargeFailedException;
import kr.hhplus.be.server.domain.point.exception.PointUseFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
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

	@Retryable(
		value = OptimisticLockingFailureException.class,
		maxAttempts = 3,
		backoff = @Backoff(delay = 1000, multiplier = 2.0f, maxDelay = 5000) // 최대 지연시간을 5초로 설정
	)
	@Transactional
	public Point chargeUserPointV2(PointChargeCommand command) {
		if (command.userId() == null) {
			throw new IllegalArgumentException("userId는 null일 수 없습니다.");
		}

		Point userPoint = pointRepository.findByUserIdWithOptimistic(command.userId());

		Point chargedUserPoint = userPoint.charge(command.chargeAmount());

		PointHistory pointHistory = PointHistory.create(userPoint, command.chargeAmount(), TransactionType.CHARGE);
		return pointRepository.saveWithHistory(chargedUserPoint, pointHistory);
	}

	@Recover
	public Point recoverCharge(OptimisticLockingFailureException e, PointChargeCommand command) {
		log.warn("포인트 충전 재시도 초과: userId={}, chargeAmount={}", command.userId(), command.chargeAmount());
		throw new PointChargeFailedException();
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

	@Retryable(
		value = OptimisticLockingFailureException.class,
		maxAttempts = 3,
		backoff = @Backoff(delay = 1000)
	)
	@Transactional
	public Point useUserPointV2(PointUseCommand command) {
		if (command.userId() == null) {
			throw new IllegalArgumentException("userId는 null일 수 없습니다.");
		}
		Point userPoint = pointRepository.findByUserIdWithOptimistic(command.userId());

		Point usedUserPoint = userPoint.use(command.useAmount());

		PointHistory pointHistory = PointHistory.create(userPoint, command.useAmount(), TransactionType.USE);
		return pointRepository.saveWithHistory(usedUserPoint, pointHistory);
	}

	@Recover
	public Point recoverUse(OptimisticLockingFailureException e, PointUseCommand command) {
		log.warn("포인트 사용 재시도 초과: userId={}, useAmount={}", command.userId(), command.useAmount());
		throw new PointUseFailedException();
	}

}
