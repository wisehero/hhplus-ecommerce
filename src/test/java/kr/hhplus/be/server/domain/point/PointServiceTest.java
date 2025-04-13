package kr.hhplus.be.server.domain.point;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import kr.hhplus.be.server.domain.point.dto.PointChargeCommand;
import kr.hhplus.be.server.domain.point.exception.PointNotEnoughException;

@ExtendWith(MockitoExtension.class)
public class PointServiceTest {

	@Mock
	private PointRepository pointRepository;

	@InjectMocks
	private PointService pointService;

	@Test
	@DisplayName("포인트를 조회한다.")
	void getPointOfUser() {
		// given
		Long userId = 1L;
		when(pointRepository.findByUserId(userId)).thenReturn(
			Point.builder()
				.userId(userId)
				.balance(Balance.createBalance(BigDecimal.valueOf(1000)))
				.build());

		// when
		Point result = pointService.getPointOfUser(userId);

		// then
		assertAll(
			() -> assertThat(result.getUserId()).isEqualTo(userId),
			() -> assertThat(result.getAmount()).isEqualTo(BigDecimal.valueOf(1000))
		);
		verify(pointRepository, times(1)).findByUserId(userId);
	}

	@Test
	@DisplayName("사용자가 없을 경우 잔액이 0인 포인트를 생성한다.")
	void createPointIfNotExists() {
		// given
		Long userId = 1L;
		when(pointRepository.findByUserId(userId)).thenReturn(null);
		when(pointRepository.save(any(Point.class))).thenReturn(Point.createZeroUserPoint(userId));

		// when
		Point result = pointService.getPointOfUser(userId);

		// then
		assertAll(
			() -> assertThat(result.getUserId()).isEqualTo(userId),
			() -> assertThat(result.getAmount()).isEqualTo(BigDecimal.ZERO)
		);
		verify(pointRepository, times(1)).findByUserId(userId);
		verify(pointRepository, times(1)).save(any(Point.class));
	}

	@Test
	@DisplayName("포인트를 충전하면 충전된 포인트 잔액을 확인할 수 있고 이력이 저장된다.")
	void chargePoint() {
		// given
		Long userId = 1L;
		BigDecimal chargeAmount = BigDecimal.valueOf(1000);
		Point userPoint = Point.builder()
			.userId(userId)
			.balance(Balance.createBalance(BigDecimal.ZERO))
			.build();

		Point chargedUserPoint = Point.builder()
			.userId(userId)
			.balance(Balance.createBalance(chargeAmount))
			.build();

		PointChargeCommand command = new PointChargeCommand(userId, chargeAmount);

		when(pointRepository.findByUserId(userId)).thenReturn(userPoint);
		when(pointRepository.saveWithHistory(any(Point.class), any(PointHistory.class)))
			.thenReturn(chargedUserPoint);

		// when
		Point result = pointService.chargeUserPoint(command);

		// then
		assertAll(
			() -> assertThat(result.getUserId()).isEqualTo(userId),
			() -> assertThat(result.getAmount()).isEqualTo(chargeAmount)
		);
		verify(pointRepository).saveWithHistory(any(Point.class), any(PointHistory.class));
	}

	@Test
	@DisplayName("포인트를 사용하면 사용한 포인트만큼 차감된 잔액을 확인할 수 있고 이력이 저장된다.")
	void usePointTest() {
		// given
		Long userId = 1L;
		BigDecimal initialBalance = BigDecimal.valueOf(1000);
		BigDecimal useAmount = BigDecimal.valueOf(500);

		Point userPoint = Point.builder()
			.userId(userId)
			.balance(Balance.createBalance(initialBalance))
			.build();

		Point updatedPoint = Point.builder()
			.userId(userId)
			.balance(Balance.createBalance(initialBalance.subtract(BigDecimal.valueOf(500))))
			.build();

		when(pointRepository.findByUserId(userId)).thenReturn(userPoint);
		when(pointRepository.saveWithHistory(any(Point.class), any(PointHistory.class))).thenReturn(updatedPoint);

		// when
		Point point = pointService.useUserPoint(userId, useAmount);

		// then
		assertAll(
			() -> assertThat(point.getUserId()).isEqualTo(userId),
			() -> assertThat(point.getAmount()).isEqualTo(initialBalance.subtract(useAmount))
		);
		verify(pointRepository, times(1)).saveWithHistory(any(Point.class), any(PointHistory.class));
	}

	@Test
	@DisplayName("포인트 잔액 부족으로 인해 결제에 실패한다면, PointNotEnoughException이 발생하고 이력도 남지 않는다.")
	void usePointFailTest() {
		// given
		Long userId = 1L;
		BigDecimal initialBalance = BigDecimal.valueOf(1000);
		BigDecimal useAmount = BigDecimal.valueOf(1001);

		Point userPoint = Point.builder()
			.userId(userId)
			.balance(Balance.createBalance(initialBalance))
			.build();

		when(pointRepository.findByUserId(userId)).thenReturn(userPoint);

		// when
		assertThatThrownBy(() -> pointService.useUserPoint(userId, useAmount))
			.isInstanceOf(PointNotEnoughException.class)
			.hasMessage("비즈니스 정책을 위반한 요청입니다.")
			.extracting("detail")
			.isEqualTo("잔액이 부족합니다. 현재 포인트 : 1000, 사용 시도 포인트 : 1001");

		verify(pointRepository, times(1)).findByUserId(userId);
		verify(pointRepository, never()).saveWithHistory(any(Point.class), any(PointHistory.class));
	}
}