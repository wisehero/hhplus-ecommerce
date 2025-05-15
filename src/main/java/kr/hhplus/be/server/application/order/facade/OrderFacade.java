package kr.hhplus.be.server.application.order.facade;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.server.application.order.dto.OrderCreateCommand;
import kr.hhplus.be.server.application.order.dto.OrderCreateResult;
import kr.hhplus.be.server.domain.bestseller.event.type.BestSellerRealTimeUpdatedEvent;
import kr.hhplus.be.server.domain.coupon.CouponService;
import kr.hhplus.be.server.domain.coupon.PublishedCoupon;
import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.OrderService;
import kr.hhplus.be.server.domain.product.Product;
import kr.hhplus.be.server.domain.product.ProductService;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.user.UserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderFacade {

	private final ProductService productService;
	private final CouponService couponService;
	private final OrderService orderService;
	private final UserService userService;
	private final ApplicationEventPublisher eventPublisher;

	@Transactional
	public OrderCreateResult createOrderV1(OrderCreateCommand command) {

		User findUser = userService.getUserById(command.userId());

		// 우선 주문 도메인 객체를 생성해
		Order order = Order.create(findUser);

		// 그리고 상품 재고를 차감하고 주문 상품을 추가해
		command.orderLines().forEach(line -> {
			Product product = productService.decreaseStockLockFree(line.productId(), line.quantity());
			order.addOrderProduct(product, line.quantity());
		});

		// 적용할 쿠폰이 있어?
		if (command.publishedCouponId() != null) {
			PublishedCoupon findPublishedCoupon = couponService.getPublishedCouponById(command.publishedCouponId());

			order.applyCoupon(findPublishedCoupon);
		}

		return OrderCreateResult.from(orderService.order(order));
	}

	/**
	 * 주문 생성 파사드 (재고 차감 시 비관적 락 사용)
	 * @param command
	 * @return
	 */
	@Transactional
	public OrderCreateResult createOrderV2(OrderCreateCommand command) {

		User findUser = userService.getUserById(command.userId());

		// 우선 주문 도메인 객체를 생성해
		Order order = Order.create(findUser);

		// 그리고 상품 재고를 차감하고 주문 상품을 추가해
		command.orderLines().forEach(line -> {
			Product product = productService.decreaseStockWithPessimistic(line.productId(), line.quantity());
			order.addOrderProduct(product, line.quantity());

			// 베스트셀러 업데이트 이벤트 발행
			eventPublisher.publishEvent(
				new BestSellerRealTimeUpdatedEvent(this, product.getId(), product.getProductName(), line.quantity()));
		});

		// 적용할 쿠폰이 있어?
		if (command.publishedCouponId() != null) {
			PublishedCoupon findPublishedCoupon = couponService.getPublishedCouponById(command.publishedCouponId());

			order.applyCoupon(findPublishedCoupon);
		}

		return OrderCreateResult.from(orderService.order(order));
	}

	@Transactional
	public OrderCreateResult createOrderV3(OrderCreateCommand command) {

		User findUser = userService.getUserById(command.userId());

		// 우선 주문 도메인 객체를 생성해
		Order order = Order.create(findUser);

		// 그리고 상품 재고를 차감하고 주문 상품을 추가해
		command.orderLines().forEach(line -> {
			Product product = productService.decreaseStockWithModifying(line.productId(), line.quantity());
			order.addOrderProduct(product, line.quantity());
		});

		// 적용할 쿠폰이 있어?
		if (command.publishedCouponId() != null) {
			PublishedCoupon findPublishedCoupon = couponService.getPublishedCouponById(command.publishedCouponId());

			order.applyCoupon(findPublishedCoupon);
		}

		return OrderCreateResult.from(orderService.order(order));
	}
}
