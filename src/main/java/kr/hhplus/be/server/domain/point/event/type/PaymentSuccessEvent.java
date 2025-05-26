package kr.hhplus.be.server.domain.point.event.type;

import java.io.Serializable;

import kr.hhplus.be.server.domain.order.dto.OrderInfo;

public class PaymentSuccessEvent implements Serializable {

	private final OrderInfo orderInfo;

	public PaymentSuccessEvent(OrderInfo orderInfo) {
		this.orderInfo = orderInfo;
	}

	public OrderInfo getOrderInfo() {
		return orderInfo;
	}
}
