package kr.hhplus.be.server.integration;

import static org.assertj.core.api.Assertions.*;
import static org.instancio.Select.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.hhplus.be.server.domain.point.Balance;
import kr.hhplus.be.server.domain.point.Point;
import kr.hhplus.be.server.domain.point.PointHistory;
import kr.hhplus.be.server.domain.point.PointRepository;
import kr.hhplus.be.server.domain.point.PointService;
import kr.hhplus.be.server.domain.point.TransactionType;
import kr.hhplus.be.server.domain.point.dto.PointChargeCommand;
import kr.hhplus.be.server.domain.point.dto.PointUseCommand;
import kr.hhplus.be.server.domain.point.exception.PointNotEnoughException;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.user.UserRepository;
import kr.hhplus.be.server.support.IntgerationTestSupport;

public class PointServiceIntgerationTest extends IntgerationTestSupport {

	@Autowired
	private PointService pointService;

	@Autowired
	private PointRepository pointRepository;

	@Autowired
	private UserRepository userRepository;

	@Test
	@DisplayName("사용자의 포인트가 없는 경우 0으로 초기화한다.")
	void getPointOfUser() {
		// given
		Long userId = 1L;

		// when
		Point point = pointService.getPointOfUser(userId);

		// then
		assertAll(
			() -> assertThat(point.getUserId()).isEqualTo(userId),
			() -> assertThat(point.getAmount()).isEqualByComparingTo(BigDecimal.ZERO)
		);
	}

	@Test
	@DisplayName("이미 포인트가 존재하는 사용자의 포인트를 조회한다.")
	void getPointOfExistUser() {
		// given
		Long userId = 1L;
		Point userPoint = Point.create(userId, Balance.createBalance(BigDecimal.TEN));
		pointRepository.save(userPoint);

		// when
		Point result = pointRepository.findByUserId(userId);

		// then
		assertAll(
			() -> assertThat(result.getUserId()).isEqualTo(userId),
			() -> assertThat(result.getAmount()).isEqualByComparingTo(BigDecimal.TEN)
		);
	}

	@Test
	@DisplayName("포인트 조회 시 사용자 ID가 null인 경우 IllegalArgumentException 예외를 발생시킨다.")
	void getPointOfUserWithNullUserId() {
		// given
		Long userId = null;

		// when
		assertThatThrownBy(() -> pointService.getPointOfUser(userId))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("userId는 null일 수 없습니다.");
	}

	@Test
	@DisplayName("포인트를 충전하면 충전한 금액만큼 잔액이 늘고 포인트 충전이력이 저장된다.")
	void chargeUserPoint() {
		// given
		Long userId = 1L;
		Point originalPoint = Point.createZeroUserPoint(userId); // 초기 포인트가 0인 객체
		BigDecimal chargeAmount = BigDecimal.valueOf(1000);
		PointChargeCommand command = new PointChargeCommand(userId, chargeAmount);

		pointRepository.save(originalPoint);

		// when
		Point result = pointService.chargeUserPoint(command);

		// then
		PointHistory pointHistory = pointRepository.getPointHistoryByPointId(result.getId()).get(0);
		assertAll(
			() -> assertThat(result.getUserId()).isEqualTo(userId),
			() -> assertThat(result.getAmount()).isEqualByComparingTo(chargeAmount),
			() -> assertThat(pointHistory.getPointId()).isEqualTo(result.getId()),
			() -> assertThat(pointHistory.getAmount()).isEqualByComparingTo(chargeAmount),
			() -> assertThat(pointHistory.getType()).isEqualTo(TransactionType.CHARGE),
			() -> assertThat(pointHistory.getBalance()).isEqualByComparingTo(result.getAmount())
		);
	}

	@Test
	@DisplayName("포인트 충전시 충전하고자하는 userId가 null이라면 IllegalArgumentException이 발생한다.")
	void chargeUserPointWithNullUserId() {
		// given
		Long userId = null;
		BigDecimal chargeAmount = BigDecimal.valueOf(1000);
		PointChargeCommand command = new PointChargeCommand(userId, chargeAmount);

		// when
		assertThatThrownBy(() -> pointService.chargeUserPoint(command))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("userId는 null일 수 없습니다.");
	}

	@Test
	@DisplayName("포인트를 사용하면 사용만큼 잔액이 줄어들고 포인트 사용 이력이 저장된다.")
	void userUserPoint() {
		// given
		Long userId = 1L;
		Point originalPoint = Point.create(userId, Balance.createBalance(BigDecimal.valueOf(1000)));
		BigDecimal useAmount = BigDecimal.valueOf(500);

		PointUseCommand command = new PointUseCommand(userId, useAmount);

		pointRepository.save(originalPoint);

		// when
		Point result = pointService.useUserPoint(command);

		// then
		PointHistory pointHistory = pointRepository.getPointHistoryByPointId(result.getId()).get(0);
		assertAll(
			() -> assertThat(result.getUserId()).isEqualTo(userId),
			() -> assertThat(result.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(500)),
			() -> assertThat(pointHistory.getPointId()).isEqualTo(result.getId()),
			() -> assertThat(pointHistory.getAmount()).isEqualByComparingTo(useAmount),
			() -> assertThat(pointHistory.getType()).isEqualTo(TransactionType.USE),
			() -> assertThat(pointHistory.getBalance()).isEqualByComparingTo(result.getAmount())
		);
	}

	@Test
	@DisplayName("포인트 충전시 충전하고자하는 userId가 null이라면 IllegalArgumentException이 발생한다.")
	void useUserPointWithNullUserId() {
		// given
		Long userId = null;
		BigDecimal useAmount = BigDecimal.valueOf(1000);
		PointUseCommand command = new PointUseCommand(userId, useAmount);

		// when
		assertThatThrownBy(() -> pointService.useUserPoint(command))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("userId는 null일 수 없습니다.");
	}

	@DisplayName("포인트 잔액이 부족하면 포인트 사용에 실패하고 PointNotEnoughException예외가 발생한다.")
	@Test
	void shouldFailWhenPointBalanceIsInsufficient() {
		// given
		User user = userRepository.save(
			Instancio.of(User.class)
				.ignore(field(User.class, "id"))
				.create()
		);

		Point point = Point.builder()
			.userId(user.getId())
			.balance(Balance.createBalance(BigDecimal.valueOf(500))) // 현재 잔액: 500원
			.build();
		pointRepository.save(point);

		PointUseCommand command = new PointUseCommand(user.getId(), BigDecimal.valueOf(1000)); // 요청: 1000원

		// when & then
		Point findPoint = pointRepository.findByUserId(user.getId());
		assertThat(findPoint.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(500));
		assertThatThrownBy(() -> pointService.useUserPoint(command))
			.isInstanceOf(PointNotEnoughException.class);
	}
}
