package kr.hhplus.be.server.application.point.dto;

public record PointOrderPaymentCommand(
	Long orderId,
	Long userId
) {
}
