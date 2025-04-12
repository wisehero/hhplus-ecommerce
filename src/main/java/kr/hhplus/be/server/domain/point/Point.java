package kr.hhplus.be.server.domain.point;

import java.math.BigDecimal;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import kr.hhplus.be.server.domain.point.exception.PointNotEnoughException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Point {

	@Id
	private Long id;

	private Long userId;

	@Embedded
	private Balance balance;

	@Builder
	private Point(Long userId, Balance balance) {
		this.userId = userId;
		this.balance = balance;
	}

	public static Point create(Long userId, Balance balance) {
		return Point.builder()
			.userId(userId)
			.balance(balance)
			.build();
	}

	public static Point createZeroUserPoint(Long userId) {
		return Point.builder()
			.userId(userId)
			.balance(new Balance(BigDecimal.ZERO))
			.build();
	}

	public Point charge(BigDecimal chargeAmount) {
		this.balance.add(chargeAmount);
		return this;
	}

	public Point use(BigDecimal useAmount) {
		if (isInsufficientBalance(useAmount)) {
			throw new PointNotEnoughException(this.getAmount(), useAmount);
		}
		this.balance.subtract(useAmount);
		return this;
	}

	private boolean isInsufficientBalance(BigDecimal useAmount) {
		return this.getAmount().compareTo(useAmount) <= 0;
	}

	public Long getId() {
		return id;
	}

	public Long getUserId() {
		return userId;
	}

	public BigDecimal getAmount() {
		return balance.getAmount();
	}
}
