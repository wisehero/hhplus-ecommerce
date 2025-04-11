package kr.hhplus.be.server.domain.point;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import kr.hhplus.be.server.domain.point.exception.PointNotEnoughException;

class PointTest {

	@Test
	@DisplayName("잔액이 0인 포인트를 생성할 수 있다.")
	void createPoint() {
		// given
		Long userId = 1L;

		// when
		Point point = Point.createZeroUserPoint(userId);

		// then
		assertAll(
			() -> assertThat(point.getUserId()).isEqualTo(userId),
			() -> assertThat(point.getAmount()).isEqualTo(BigDecimal.ZERO)
		);
	}

	@Test
	@DisplayName("포인트를 충전하면 잔액이 증가한다.")
	void chargePoint() {
		// given
		Long userId = 1L;
		Point userPoint = Point.createZeroUserPoint(userId);
		BigDecimal chargeAmount = BigDecimal.valueOf(1000);

		// when
		Point chargedPoint = userPoint.charge(chargeAmount);

		// then
		BigDecimal afterChargedBalance = chargedPoint.getAmount();
		assertThat(afterChargedBalance).isEqualTo(BigDecimal.valueOf(1000));
	}

	@Test
	@DisplayName("포인트를 사용하면 잔액이 차감된다.")
	void usePoint() {
		// given
		Long userId = 1L;
		Point userPoint = Point.create(userId, new Balance(BigDecimal.valueOf(1000)));
		BigDecimal useAmount = BigDecimal.valueOf(500);

		// when
		Point usedPoint = userPoint.use(useAmount);

		// then
		BigDecimal afterUsedBalance = usedPoint.getAmount();
		assertThat(afterUsedBalance).isEqualTo(BigDecimal.valueOf(500));
	}

	@Test
	@DisplayName("포인트 사용 시 잔액이 부족하면 PointNotEnoughException이 발생한다.")
	void usePointNotEnoughException() {
		// given
		Long userId = 1L;
		Point userPoint = Point.create(userId, new Balance(BigDecimal.valueOf(1000)));

		// expected
		assertThatThrownBy(() -> userPoint.use(BigDecimal.valueOf(1001)))
			.isInstanceOf(PointNotEnoughException.class)
			.hasMessage("비즈니스 정책을 위반한 요청입니다.")
			.extracting("detail")
			.isEqualTo("포인트 잔액이 부족합니다. 현재 포인트 : 1000, 사용 시도 포인트 : 1001");

	}
}