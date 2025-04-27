package kr.hhplus.be.server.domain.point;

import java.math.BigDecimal;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "point")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Point {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long userId;

	@Embedded
	private Balance balance;

	@Version
	private Long version;

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
		this.balance.subtract(useAmount);
		return this;
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
