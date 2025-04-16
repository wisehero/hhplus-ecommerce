package kr.hhplus.be.server.domain.point.dto;

import java.math.BigDecimal;

public record PointUseCommand(
	Long userId, BigDecimal useAmount
) {

	public static PointUseCommand of(Long userId, BigDecimal useAmount) {
		return new PointUseCommand(userId, useAmount);
	}
}
