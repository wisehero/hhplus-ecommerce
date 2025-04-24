package kr.hhplus.be.server.interfaces.api.point.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import kr.hhplus.be.server.application.point.dto.PointOrderPaymentCommand;

@Schema(description = "사용자 포인트 사용 요청 DTO")
public record PointUsageRequest(
	@Schema(
		description = "포인트를 사용할 주문 ID",
		example = "1"
	)
	@NotNull(message = "주문 ID는 필수입니다.")
	Long orderId,
	@Schema(
		description = "포인트를 사용할 사용자 ID",
		example = "1"
	)
	@NotNull(message = "사용자 ID는 필수입니다.")
	Long userId
) {

	public PointOrderPaymentCommand toCommand() {
		return new PointOrderPaymentCommand(
			this.orderId,
			this.userId
		);
	}
}
