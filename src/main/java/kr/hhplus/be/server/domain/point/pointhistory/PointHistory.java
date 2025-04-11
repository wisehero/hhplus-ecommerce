package kr.hhplus.be.server.domain.point.pointhistory;

import java.math.BigDecimal;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import kr.hhplus.be.server.domain.point.Balance;
import kr.hhplus.be.server.domain.point.Point;
import kr.hhplus.be.server.domain.point.TransactionType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class PointHistory {

	@Id
	private Long id;

	private Long pointId;

	private BigDecimal amount;

	private BigDecimal balance;

	private TransactionType type;

	@Builder
	private PointHistory(Long id, Long pointId, BigDecimal amount, BigDecimal balance, TransactionType type) {
		this.id = id;
		this.pointId = pointId;
		this.amount = amount;
		this.balance = balance;
		this.type = type;
	}

	public static PointHistory createPointHistory(Point point, BigDecimal amount, TransactionType type) {
		return PointHistory.builder()
			.pointId(point.getId())
			.amount(amount)
			.balance(point.getAmount())
			.type(type)
			.build();
	}
}
