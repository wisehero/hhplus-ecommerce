package kr.hhplus.be.server.interfaces.api.order.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "주문 상품 정보")
public record OrderProductRequest(
	@Schema(description = "주문할 상품 ID", example = "1")
	Long productId,
	@Schema(description = "주문할 상품 수량, 양의 정수", example = "10")
	Long quantity
) {
}
