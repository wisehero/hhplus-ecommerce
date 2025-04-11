package kr.hhplus.be.server.domain.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

	private final OrderRepository orderRepository;
	private final OrderProductRepository orderProductRepository;

	public Order getOrderById(Long orderId) {
		return orderRepository.findById(orderId);
	}

	@Transactional
	public Order order(Long userId, Long userCouponId, List<OrderProduct> orderProducts, BigDecimal totalPrice) {
		Order order = Order.create(userId, userCouponId, totalPrice);
		Order savedOrder = orderRepository.save(order);

		orderProducts.forEach(op -> op.assignOrderId(savedOrder.getId()));
		orderProductRepository.saveAll(orderProducts);

		return savedOrder;
	}

	@Transactional
	public void completeOrder(Long orderId) {
		Order order = orderRepository.findById(orderId);
		order.paid();
	}

	@Transactional
	public Order expireOrder(Long orderId) {
		Order order = orderRepository.findById(orderId);
		order.expire();
		return order;
	}

	public List<OrderProduct> getOrderProducts(Long orderId) {
		return orderProductRepository.findByOrderId(orderId);
	}

	public List<Order> getOverDueOrderIds(LocalDateTime deadLine) {
		return orderRepository.findAllPendingBefore(OrderStatus.PENDING, deadLine);
	}
}
