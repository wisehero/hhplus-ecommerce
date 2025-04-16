package kr.hhplus.be.server.domain.point;

import java.math.BigDecimal;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import kr.hhplus.be.server.domain.point.exception.PointNotEnoughException;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor
@AttributeOverrides({
	@AttributeOverride(name = "amount", column = @Column(name = "balance"))
})
public class Balance {

	private BigDecimal amount;

	public Balance(BigDecimal balance) {
		if (balance == null || balance.compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("잔액은 null이 아니며, 0보다 크거나 같아야 합니다. 잔액 : %s".formatted(balance));
		}
		this.amount = balance;
	}

	public static Balance createBalance(BigDecimal balance) {
		return new Balance(balance);
	}

	public void add(BigDecimal chargeAmount) {
		if (chargeAmount == null || chargeAmount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("충전 금액은 null이 아니며, 0보다 커야 합니다. 충전 시도 금액 : %s".formatted(chargeAmount));
		}
		this.amount = this.amount.add(chargeAmount);
	}

	public void subtract(BigDecimal useAmount) {
		if (useAmount == null || useAmount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("사용 금액은 null이 아니며, 0보다 커야 합니다. 사용 시도 금액 : %s".formatted(useAmount));
		}

		if (this.amount.compareTo(useAmount) < 0) {
			throw new PointNotEnoughException(this.amount, useAmount);
		}
		this.amount = this.amount.subtract(useAmount);
	}

	public BigDecimal getAmount() {
		return amount;
	}
}
