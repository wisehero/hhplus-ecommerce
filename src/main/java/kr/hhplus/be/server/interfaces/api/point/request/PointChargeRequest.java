package kr.hhplus.be.server.interfaces.api.point.request;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import kr.hhplus.be.server.domain.point.dto.PointChargeCommand;

@Schema(description = "사용자 포인트 충전 요청 DTO")
public record PointChargeRequest(
	@Schema(
		description = "사용자 ID, 1 이상의 정수",
		example = "123"
	)
	@NotNull(message = "사용자 ID는 필수입니다.")
	Long userId,

	@Schema(
		description = "충전할 포인트, 1과 1,000,000 사이의 정수",
		example = "10000"
	)
	@Positive(message = "충전할 포인트는 정수여야 합니다.")
	BigDecimal chargeAmount
) {

	public PointChargeCommand toCommand() {
		return new PointChargeCommand(userId, chargeAmount);
	}
}
