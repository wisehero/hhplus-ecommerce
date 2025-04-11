package kr.hhplus.be.server.domain.order;

import java.util.List;

public interface OrderProductRepository {

	List<OrderProduct> findByOrderId(Long orderId);

	OrderProduct save(OrderProduct orderProduct);

	void saveAll(List<OrderProduct> orderProducts);
}
