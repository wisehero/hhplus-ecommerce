package kr.hhplus.be.server.application.order.dto;

import java.util.List;

public record OrderCreateCommand(
	Long userId,
	Long publishedCouponId,
	List<OrderLine> orderLines
) {

}
