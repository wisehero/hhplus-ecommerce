package kr.hhplus.be.server.domain.order.client;

import kr.hhplus.be.server.domain.order.dto.OrderInfo;

public interface DataPlatformClient {

	boolean send(OrderInfo order);
}
