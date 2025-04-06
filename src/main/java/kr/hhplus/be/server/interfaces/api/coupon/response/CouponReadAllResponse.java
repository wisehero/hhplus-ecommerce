package kr.hhplus.be.server.interfaces.api.coupon.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "쿠폰 전체 조회 응답 DTO")
public record CouponReadAllResponse(
	@Schema(description = "쿠폰을 발급받은 사용자 ID", example = "1")
	Long userId,
	@Schema(description = "쿠폰 리스트")
	List<CouponInfo> coupons
) {
}
