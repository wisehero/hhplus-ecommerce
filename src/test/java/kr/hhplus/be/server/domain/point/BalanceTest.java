package kr.hhplus.be.server.domain.point;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

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