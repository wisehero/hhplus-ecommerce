package kr.hhplus.be.server.application.order.facade;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.Transient;
import kr.hhplus.be.server.application.order.dto.OrderCreateCommand;
import kr.hhplus.be.server.application.order.dto.OrderCreateResult;
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

	@Transactional
	public OrderCreateResult createOrder(OrderCreateCommand command) {

		User findUser = userService.getUserById(command.userId());

		// 우선 주문 도메인 객체를 생성해
		Order order = Order.create(findUser);

		// 그리고 상품 재고를 차감하고 주문 상품을 추가해
		command.orderLines().forEach(line -> {
			Product product = productService.getProductById(line.productId());
			productService.decreaseStock(product, line.quantity());
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
