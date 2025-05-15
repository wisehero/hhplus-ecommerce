package kr.hhplus.be.server.infra.order;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.OrderProduct;
import kr.hhplus.be.server.domain.order.OrderRepository;
import kr.hhplus.be.server.domain.order.OrderStatus;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

	private final OrderJpaRepository orderJpaRepository;
	private final OrderProductJpaRepository orderProductJpaRepository;

	@Override
	public Order findOrderById(Long orderId) {
		Order findOrder = orderJpaRepository.findById(orderId)
			.orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
		List<OrderProduct> orderProducts = orderProductJpaRepository.findAllByOrderId(orderId);
		findOrder.assignOrderProduct(orderProducts);
		return findOrder;
	}

	@Override
	public Order save(Order order) {
		Order savedOrder = orderJpaRepository.save(order);

		order.getOrderProducts().forEach(op -> op.assignOrderId(savedOrder.getId()));

		orderProductJpaRepository.saveAll(order.getOrderProducts());
		return savedOrder;
	}

	@Override
	public List<Order> findAllPendingBefore(OrderStatus orderStatus, LocalDateTime deadline) {
		List<Order> orders = orderJpaRepository.findAllPendingBefore(orderStatus, deadline);
		orders.forEach(order -> {
			List<OrderProduct> orderProducts = orderProductJpaRepository.findAllByOrderId(order.getId());
			order.assignOrderProduct(orderProducts);
		});
		return orders;
	}

	@Override
	public List<OrderProduct> findOrderProductsByOrderId(Long orderId) {
		return orderProductJpaRepository.findAllByOrderId(orderId);
	}

	@Override
	public List<Order> findPaidOrdersWithinOneHour(LocalDateTime now) {
		return orderJpaRepository.findPaidOrdersWithinOneHour(now.minusHours(1), now)
			.stream()
			.peek(order -> {
				List<OrderProduct> orderProducts = orderProductJpaRepository.findAllByOrderId(order.getId());
				order.assignOrderProduct(orderProducts);
			}).toList();
	}
}
