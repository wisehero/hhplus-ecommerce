package kr.hhplus.be.server.domain.order.client;

import kr.hhplus.be.server.domain.order.Order;

public interface DataPlatformClient {

	boolean send(Order order);
}
