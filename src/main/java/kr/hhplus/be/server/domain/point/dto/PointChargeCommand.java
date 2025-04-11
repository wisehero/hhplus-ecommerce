package kr.hhplus.be.server.domain.point.dto;

import java.math.BigDecimal;

public record PointChargeCommand(
	Long userId,
	BigDecimal chargeAmount
) {

}
