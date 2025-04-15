package kr.hhplus.be.server.interfaces.api.coupon.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import kr.hhplus.be.server.domain.coupon.dto.CouponIssueCommand;

@Schema(description = "쿠폰 발급 요청 DTO")
public record CouponIssueRequest(
	@Schema(description = "쿠폰 발급을 요청하는 사용자 ID", example = "1")
	@NotNull(message = "사용자 ID는 필수입니다.")
	Long userId,
	@Schema(description = "발급받을 쿠폰 ID", example = "1")
	@NotNull(message = "쿠폰 ID는 필수입니다.")
	Long couponId
) {

	public CouponIssueCommand toIssueCommand() {
		return new CouponIssueCommand(
			userId,
			couponId
		);
	}
}
