package kr.hhplus.be.server.domain.order;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository {

	Order findOrderById(Long orderId);

	Order save(Order order);

	List<Order> findAllPendingBefore(OrderStatus orderStatus, LocalDateTime deadLine);

	List<OrderProduct> findOrderProductsByOrderId(Long orderId);

	List<Order> findPaidOrdersWithinOneHour(LocalDateTime now);

}
