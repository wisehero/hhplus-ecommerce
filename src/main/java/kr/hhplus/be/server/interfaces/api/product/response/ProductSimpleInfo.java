package kr.hhplus.be.server.interfaces.api.product.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "상품 간단 정보")
public record ProductSimpleInfo(
	@Schema(name = "상품 ID", example = "1")
	Long productId,
	@Schema(name = "상품 이름", example = "상품1")
	String productName,
	@Schema(name = "가격", example = "100000")
	BigDecimal price,
	@Schema(name = "재고", example = "1000")
	Long stock
) {
}
