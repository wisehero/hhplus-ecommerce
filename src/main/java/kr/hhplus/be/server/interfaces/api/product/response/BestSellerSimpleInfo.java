package kr.hhplus.be.server.interfaces.api.product.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
	description = "베스트셀러 상품 정보",
	example = """
			{
				"productId": 1,
				"productName": "상품1",
				"salesCount": 1000,
				"stock": 10000,
				"price": 100000
			}
		""")
public record BestSellerSimpleInfo(
	@Schema(name = "상품 ID", example = "1")
	Long productId,
	@Schema(name = "상품 이름", example = "상품1")
	String productName,
	@Schema(name = "판매량", example = "1000")
	Long salesCount,
	@Schema(name = "재고", example = "10000")
	Long stock,
	@Schema(name = "가격", example = "100000")
	BigDecimal price
) {
}
