package kr.hhplus.be.server.application.order.dto;

import java.math.BigDecimal;
import java.util.List;

import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.interfaces.api.order.request.OrderLine;

public record OrderCreateCommand(
	Long userId,
	Long userCouponId,
	List<OrderLine> orderLines
) {

	public static Order toOrder(Long userId, Long userCouponId, BigDecimal totalPrice) {
		return Order.create(userId, userCouponId, totalPrice);
	}
}
