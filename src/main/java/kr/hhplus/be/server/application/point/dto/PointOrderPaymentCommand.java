package kr.hhplus.be.server.application.point.dto;

import java.math.BigDecimal;

public record PointOrderPaymentCommand(
	Long orderId,
	Long userId
) {
}
