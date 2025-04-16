package kr.hhplus.be.server.domain.order;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import kr.hhplus.be.server.domain.product.Product;

class OrderProductTest {

	@Test
	@DisplayName("정상적인 상품과 수량으로 OrderProduct를 생성할 수 있다")
	void createOrderProductSuccessfully() {
		// given
		Long productId = 1L;
		String productName = "맥북";
		BigDecimal price = BigDecimal.valueOf(2000000);
		Long quantity = 2L;

		Product product = Instancio.of(Product.class)
			.set(Select.field("id"), productId)
			.set(Select.field("productName"), productName)
			.set(Select.field("price"), price)
			.create();

		// when
		OrderProduct orderProduct = OrderProduct.create(product, quantity);

		// then
		assertAll(
			() -> assertThat(orderProduct.getProductId()).isEqualTo(productId),
			() -> assertThat(orderProduct.getProductName()).isEqualTo(productName),
			() -> assertThat(orderProduct.getUnitPrice()).isEqualByComparingTo(price),
			() -> assertThat(orderProduct.getQuantity()).isEqualTo(quantity),
			() -> assertThat(orderProduct.getSubTotalPrice()).isEqualByComparingTo(
				price.multiply(BigDecimal.valueOf(quantity)))
		);
	}

	@Test
	@DisplayName("Order ID를 할당하면 orderId가 세팅된다")
	void assignOrderIdSetsOrderId() {
		// given
		Long orderId = 1L;
		Product product = Instancio.of(Product.class)
			.set(Select.field("id"), 1L)
			.create();
		OrderProduct orderProduct = OrderProduct.create(product, 2L);

		// when
		orderProduct.assignOrderId(orderId);

		// then
		assertThat(orderProduct.getOrderId()).isEqualTo(orderId);
	}
}