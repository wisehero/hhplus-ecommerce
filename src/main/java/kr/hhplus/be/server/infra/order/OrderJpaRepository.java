package kr.hhplus.be.server.infra.order;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.OrderStatus;

public interface OrderJpaRepository extends JpaRepository<Order, Long> {

	@Query(
		"SELECT o FROM Order o WHERE o.orderStatus = :orderStatus AND o.orderedAt < :deadline"
	)
	List<Order> findAllPendingBefore(@Param("orderStatus") OrderStatus orderStatus,
		@Param("deadline") LocalDateTime deadline);

	@Query("SELECT o FROM Order o WHERE o.orderStatus = 'PAID' AND o.orderedAt >= :oneHourAgo AND o.orderedAt < :now")
	List<Order> findPaidOrdersWithinOneHour(@Param("oneHourAgo") LocalDateTime oneHourAgo,
		@Param("now") LocalDateTime now);
}
