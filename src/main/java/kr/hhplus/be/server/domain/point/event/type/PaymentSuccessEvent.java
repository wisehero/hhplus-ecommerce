package kr.hhplus.be.server.domain.point.event.type;

import org.springframework.context.ApplicationEvent;

import kr.hhplus.be.server.domain.order.Order;

public class PaymentSuccessEvent{

	private final Order order;

	public PaymentSuccessEvent( Order order) {
		this.order = order;
	}

	public Order getOrder() {
		return order;
	}
}
