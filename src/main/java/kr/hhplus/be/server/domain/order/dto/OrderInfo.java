package kr.hhplus.be.server.domain.order.dto;

import java.math.BigDecimal;

import kr.hhplus.be.server.domain.order.Order;

public record OrderInfo(
	Long orderId,
	Long userId,
	Long publishedCouponId,
	String orderStatus,
	BigDecimal totalPrice,
	BigDecimal discountedPrice
) {

	public OrderInfo(Order order) {
		this(
			order.getId(),
			order.getUserId(),
			order.getPublishedCouponId(),
			order.getOrderStatus().name(),
			order.getTotalPrice(),
			order.getDiscountedPrice()
		);
	}
}
