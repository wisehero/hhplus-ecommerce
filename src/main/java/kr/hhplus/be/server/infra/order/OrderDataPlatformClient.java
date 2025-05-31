package kr.hhplus.be.server.infra.order;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.client.DataPlatformClient;
import kr.hhplus.be.server.domain.order.dto.OrderInfo;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OrderDataPlatformClient implements DataPlatformClient {
	@Override
	@Retryable(
		value = {SocketTimeoutException.class, ConnectException.class, RuntimeException.class}, // 재시도할 예외
		maxAttempts = 3,
		backoff = @Backoff(delay = 1000L)
	)
	public boolean send(OrderInfo orderInfo) {
		try {
			Thread.sleep(1000L);
			log.info("주문 전송됨. 주문 ID: {}, 총액: {}", orderInfo.orderId(), orderInfo.totalPrice());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return true;
	}

	@Recover
	public boolean recoverSend(Exception e, Order order) {
		log.error("최종 데이터 전송 실패 - 주문 ID={}, 오류={}", order.getId(), e.toString());
		saveFailedOrderData(order, e);
		return false;
	}

	private void saveFailedOrderData(Order order, Exception e) {
		log.info("실패한 주문 데이터 저장 - 주문 ID={}", order.getId());
		// 이벤트에 처리 실패한 것을 DB에 기록하거나 DLQ에 저장하는 로직??
	}
}
