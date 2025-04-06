package kr.hhplus.be.server.interfaces.api.order.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "주문 생성 요청 DTO", description = "주문 생성 요청 DTO")
public record OrderCreateRequest(
	@Schema(description = "주문 생성을 요청하는 사용자 ID", example = "1")
	Long userId,
	@Schema(description = "사용할 쿠폰 ID (nullable)", example = "1")
	Long userCouponId,
	@Schema(description = "주문할 상품 목록")
	List<OrderProduct> orderProducts
) {
}
