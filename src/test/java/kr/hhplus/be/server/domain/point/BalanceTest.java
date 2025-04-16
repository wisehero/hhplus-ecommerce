package kr.hhplus.be.server.domain.point;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import kr.hhplus.be.server.domain.point.exception.PointNotEnoughException;

class BalanceTest {

	@Test
	@DisplayName("잔액을 더할 수 있다.")
	void userCanChargePoint() {
		// given
		Balance balance = new Balance(BigDecimal.valueOf(0));

		// when
		balance.add(BigDecimal.valueOf(2000));

		// then
		assertThat(balance.getAmount()).isEqualTo(BigDecimal.valueOf(2000));
	}

	@Test
	@DisplayName("잔액은 차감할 수 있다.")
	void userCanUsePoint() {
		// given
		Balance balance = new Balance(BigDecimal.valueOf(2000));

		// when
		balance.subtract(BigDecimal.valueOf(1000));

		// then
		assertThat(balance.getAmount()).isEqualTo(BigDecimal.valueOf(1000));
	}

	@Test
	@DisplayName("잔액을 차감할 때 잔액보다 큰 금액을 차감하면 PointNotEnoughException 예외가 발생한다.")
	void userCannotUseMoreThanBalance() {
		// given
		Balance balance = new Balance(BigDecimal.valueOf(1000));
		BigDecimal useAmount = BigDecimal.valueOf(2000);

		// expected
		assertThatThrownBy(() -> balance.subtract(useAmount))
			.isInstanceOf(PointNotEnoughException.class)
			.hasMessage("비즈니스 정책을 위반한 요청입니다.")
			.extracting("detail")
			.isEqualTo("잔액이 부족합니다. 현재 포인트 : 1000, 사용 시도 포인트 : 2000");
	}

	@ParameterizedTest
	@MethodSource("provideInvalidBalance")
	@DisplayName("잔액을 생성할 때 입력값이 0보다 작다면 IllegalArgumentException 예외가 발생한다.")
	void userCannotChargeNegativePoint(BigDecimal invalidBalance) {
		// expected
		assertThatThrownBy(() -> new Balance(invalidBalance))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("잔액은 null이 아니며, 0보다 크거나 같아야 합니다. 잔액 : %s".formatted(invalidBalance));
	}

	static Stream<BigDecimal> provideInvalidBalance() {
		return Stream.of(null, BigDecimal.valueOf(-1000));
	}
}