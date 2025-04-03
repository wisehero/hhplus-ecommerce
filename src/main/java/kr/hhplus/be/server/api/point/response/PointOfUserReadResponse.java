package kr.hhplus.be.server.api.point.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자 포인트 조회 응답 DTO")
public record PointOfUserReadResponse(

	@Schema(description = "사용자 ID", example = "123")
	Long userId,
	@Schema(description = "사용자의 현재 보유 포인트", example = "10000")
	Long balance
) {
}
