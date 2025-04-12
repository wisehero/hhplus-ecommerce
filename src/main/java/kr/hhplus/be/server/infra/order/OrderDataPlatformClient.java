package kr.hhplus.be.server.infra.order;

import org.springframework.stereotype.Component;

import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.client.DataPlatformClient;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OrderDataPlatformClient implements DataPlatformClient {
	@Override
	public boolean send(Order order) {
		try {
			Thread.sleep(2000L);
			log.info("주문 전송됨. 주문 ID: {}, 총액: {}", order.getId(), order.getTotalPrice());
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		return true;
	}
}
