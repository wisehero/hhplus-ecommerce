package kr.hhplus.be.server.api.point.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자 포인트 사용 요청 DTO")
public record PointUseRequest(
	@Schema(
		description = "포인트를 사용할 주문 ID",
		example = "1"
	)
	Long orderId
) {
}
