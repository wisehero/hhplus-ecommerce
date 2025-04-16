package kr.hhplus.be.server.application.order.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.server.domain.coupon.CouponService;
import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.OrderProduct;
import kr.hhplus.be.server.domain.order.OrderService;
import kr.hhplus.be.server.domain.product.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderExpireScheduler {

	private final OrderService orderService;
	private final ProductService productService;
	private final CouponService couponService;

	/**
	 * 매 1분마다 실행되며 5분 이상 결제되지 않은 주문을 만료시킴
	 */
	@Scheduled(fixedDelay = 60_000) // 1분 마다 실행
	@Transactional
	public void expireOrderThenRestoreCouponAndStock() {
		LocalDateTime deadLine = LocalDateTime.now().minusMinutes(5);
		List<Order> expireTargetOrders = orderService.getOverDueOrderIds(deadLine);

		for (Order order : expireTargetOrders) {
			try {
				Order expireOrder = orderService.expireOrder(order);

				List<OrderProduct> orderProducts = orderService.getOrderProducts(expireOrder.getId());
				for (OrderProduct orderProduct : orderProducts) {
					productService.restoreStock(orderProduct.getProductId(), orderProduct.getQuantity());
				}

				if (expireOrder.isCouponApplied()) {
					couponService.restorePublishedCoupon(expireOrder.getPublishedCouponId());
				}
			} catch (Exception e) {
				log.warn("[주문 만료 실패] orderId={}, message={}", order.getId(), e.getMessage());
			}
		}
	}
}
