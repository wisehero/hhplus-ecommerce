package kr.hhplus.be.server.application.order.dto;

import java.math.BigDecimal;

import kr.hhplus.be.server.domain.order.Order;

public record OrderCreateResult(
	Long orderId,
	Long userId,
	String orderStatus,
	BigDecimal orderTotalAmount
) {

	public static OrderCreateResult from(Order order) {
		return new OrderCreateResult(
			order.getId(),
			order.getUserId(),
			order.getOrderStatus().name(),
			order.getTotalPrice()
		);
	}
}
