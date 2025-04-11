package kr.hhplus.be.server.interfaces.api.point.request;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.hhplus.be.server.application.point.dto.PointOrderPaymentCommand;

@Schema(description = "사용자 포인트 사용 요청 DTO")
public record PointUsageRequest(
	@Schema(
		description = "포인트를 사용할 주문 ID",
		example = "1"
	)
	Long orderId,
	@Schema(
		description = "포인트를 사용할 사용자 ID",
		example = "1"
	)
	Long userId,
	@Schema(
		description = "사용할 포인트 금액",
		example = "10000"
	)
	BigDecimal useAmount
) {

	public PointOrderPaymentCommand toCommand() {
		return new PointOrderPaymentCommand(
			this.orderId,
			this.userId
		);
	}
}
