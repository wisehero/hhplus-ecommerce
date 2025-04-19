package kr.hhplus.be.server.integration;

import static org.assertj.core.api.Assertions.*;
import static org.instancio.Select.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.hhplus.be.server.application.order.dto.OrderCreateCommand;
import kr.hhplus.be.server.application.order.dto.OrderCreateResult;
import kr.hhplus.be.server.application.order.dto.OrderLine;
import kr.hhplus.be.server.application.order.facade.OrderFacade;
import kr.hhplus.be.server.domain.coupon.Coupon;
import kr.hhplus.be.server.domain.coupon.CouponRepository;
import kr.hhplus.be.server.domain.coupon.PublishedCoupon;
import kr.hhplus.be.server.domain.coupon.discountpolicy.DiscountType;
import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.OrderRepository;
import kr.hhplus.be.server.domain.product.Product;
import kr.hhplus.be.server.domain.product.ProductRepository;
import kr.hhplus.be.server.domain.product.exception.ProductOutOfStockException;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.user.UserRepository;
import kr.hhplus.be.server.support.IntgerationTestSupport;

public class OrderFacadeIntegrationTest extends IntgerationTestSupport {

	@Autowired
	private OrderFacade orderFacade;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private CouponRepository couponRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private UserRepository userRepository;

	@Test
	@DisplayName("유효한 유저, 상품, 쿠폰으로 주문 생성 시 정상적으로 주문이 생성되고, 재고가 차감되며 할인도 적용된다.")
	void shouldCreateOrderWithCouponAndApplyDiscount() {
		// given

		// 유효한 사용자
		User user = userRepository.save(Instancio.of(User.class)
			.ignore(field("id"))
			.create());

		// 주문할 상품
		Product product = productRepository.save(Instancio.of(Product.class)
			.ignore(field("id"))
			.set(field("price"), BigDecimal.valueOf(10000))
			.set(field("stock"), 100L)
			.create());

		// 적용할 쿠폰 원장
		Coupon coupon = couponRepository.save(Coupon.createLimited(
			"할인쿠폰", BigDecimal.valueOf(3000), DiscountType.FIXED, 10L,
			LocalDate.now().minusDays(1), LocalDate.now().plusDays(1)
		));

		// 사용자가 가지고 있는 쿠폰 정의
		PublishedCoupon publishedCoupon = PublishedCoupon.create(user.getId(), coupon, LocalDate.now());
		couponRepository.savePublishedCoupon(publishedCoupon);

		OrderCreateCommand command = new OrderCreateCommand(
			user.getId(),
			publishedCoupon.getId(),
			List.of(new OrderLine(product.getId(), 2L)) // 2 x 10,000 = 20,000
		);

		// when
		OrderCreateResult result = orderFacade.createOrder(command);

		// then
		Order order = orderRepository.findOrderById(result.orderId());
		assertAll(
			() -> assertThat(order.getUserId()).isEqualTo(user.getId()), // 주문의 사용자 ID == 사용자 ID
			() -> assertThat(order.getOrderProducts()).hasSize(1),
			() -> assertThat(order.getTotalPrice()).isEqualByComparingTo("20000"),
			() -> assertThat(order.getDiscountedPrice()).isEqualByComparingTo("17000"),
			() -> assertThat(order.getPublishedCouponId()).isEqualTo(publishedCoupon.getId()), // 발급된 쿠폰 적용 확인
			() -> assertThat(productRepository.findById(product.getId()).getStock()).isEqualTo(98L), // 재고 차감 확인
			() -> assertThat(couponRepository.findPublishedCouponById(publishedCoupon.getId()).isUsed()).isTrue()
		);
	}

	@Test
	@DisplayName("쿠폰 없이 주문 생성 시 할인 없이 총액만 계산되며 주문이 정상 생성된다.")
	void shouldCreateOrderWithoutCoupon() {
		// given
		User user = userRepository.save(Instancio.of(User.class)
			.ignore(field("id"))
			.create());

		Product product = productRepository.save(Instancio.of(Product.class)
			.ignore(field("id"))
			.set(field("price"), BigDecimal.valueOf(15000))
			.set(field("stock"), 50L)
			.create());

		OrderCreateCommand command = new OrderCreateCommand(
			user.getId(),
			null, // 쿠폰 없음
			List.of(new OrderLine(product.getId(), 3L)) // 3 x 15000 = 45000
		);

		// when
		OrderCreateResult result = orderFacade.createOrder(command);

		// then
		Order order = orderRepository.findOrderById(result.orderId());

		assertAll(
			() -> assertThat(order.getUserId()).isEqualTo(user.getId()),
			() -> assertThat(order.getOrderProducts()).hasSize(1),
			() -> assertThat(order.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(45000)),
			() -> assertThat(order.getDiscountedPrice()).isEqualByComparingTo(BigDecimal.ZERO),
			() -> assertThat(order.getPublishedCouponId()).isNull(),
			() -> assertThat(productRepository.findById(product.getId()).getStock()).isEqualTo(47L)
		);
	}

	@Test
	@DisplayName("상품 재고가 부족하면 주문 생성에 실패한다")
	void shouldFailWhenStockIsInsufficient() {
		// given
		User user = userRepository.save(
			Instancio.of(User.class)
				.ignore(field(User.class, "id"))
				.create()
		);

		Product product = Instancio.of(Product.class)
			.ignore(field(Product.class, "id"))
			.set(field(Product.class, "stock"), 1L) // 재고는 1개
			.create();
		product = productRepository.save(product);

		OrderLine orderLine = new OrderLine(product.getId(), 5L); // 5개 요청

		OrderCreateCommand command = new OrderCreateCommand(
			user.getId(),
			null,
			List.of(orderLine)
		);

		// when & then
		assertThatThrownBy(() -> orderFacade.createOrder(command))
			.isInstanceOf(ProductOutOfStockException.class);
	}
}
