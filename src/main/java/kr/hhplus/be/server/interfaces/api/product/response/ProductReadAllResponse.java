package kr.hhplus.be.server.interfaces.api.product.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "상품 목록 조회 응답")
public record ProductReadAllResponse(
	@Schema(name = "상품 목록", description = "상품 목록")
	List<ProductSimpleInfo> products
) {
}
