package kr.hhplus.be.server.application.order.dto;

public record OrderLine(
	Long productId,
	Long quantity
) {
}
