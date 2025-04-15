package kr.hhplus.be.server.domain.order;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

	private final OrderRepository orderRepository;

	public Order order(Order order) {
		return orderRepository.save(order);
	}

	@Transactional
	public void completeOrder(Order order) {
		order.paid();
		orderRepository.save(order);
	}

	@Transactional
	public Order expireOrder(Order order) {
		order.expire();
		return orderRepository.save(order);
	}

	public Order getOrderById(Long orderId) {
		if (orderId == null)
			throw new IllegalArgumentException("주문 ID는 null일 수 없습니다.");
		return orderRepository.findOrderById(orderId);
	}

	public List<OrderProduct> getOrderProducts(Long orderId) {
		if (orderId == null)
			throw new IllegalArgumentException("주문 ID는 null일 수 없습니다.");
		return orderRepository.findOrderProductsByOrderId(orderId);
	}

	public List<Order> getOverDueOrderIds(LocalDateTime deadLine) {
		if (deadLine == null)
			throw new IllegalArgumentException("마감 기한은 null일 수 없습니다.");
		return orderRepository.findAllPendingBefore(OrderStatus.PENDING, deadLine);
	}
}
