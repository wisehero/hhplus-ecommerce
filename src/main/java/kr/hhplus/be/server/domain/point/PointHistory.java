package kr.hhplus.be.server.domain.point;

import static jakarta.persistence.GenerationType.*;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "point_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class PointHistory {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	private Long pointId;

	private BigDecimal amount;

	private BigDecimal balance;

	@Enumerated(EnumType.STRING)
	private TransactionType type;

	@Builder
	private PointHistory(Long id, Long pointId, BigDecimal amount, BigDecimal balance, TransactionType type) {
		this.id = id;
		this.pointId = pointId;
		this.amount = amount;
		this.balance = balance;
		this.type = type;
	}

	public static PointHistory create(Point point, BigDecimal amount, TransactionType type) {
		return PointHistory.builder()
			.pointId(point.getId())
			.amount(amount)
			.balance(point.getAmount())
			.type(type)
			.build();
	}
}
