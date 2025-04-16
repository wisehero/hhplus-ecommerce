package kr.hhplus.be.server.domain.product;

import static org.assertj.core.api.Assertions.*;

import java.util.stream.Stream;

import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import kr.hhplus.be.server.domain.product.exception.ProductOutOfStockException;

class ProductTest {

	@Test
	@DisplayName("상품의 재고를 감소시킨다.")
	void reduceStockSuccess() {
		// given
		Product product = Instancio.of(Product.class)
			.set(Select.field(Product::getStock), 10L)
			.create();

		// when
		product.decreaseStock(3L);

		// then
		assertThat(product.getStock()).isEqualTo(7L);
	}

	@Test
	@DisplayName("재고 증가 성공 시 재고가 올바르게 증가한다.")
	void increaseStock_success() {
		// given
		Product product = Instancio.of(Product.class)
			.set(Select.field(Product::getStock), 5L)
			.create();

		// when
		product.increaseStock(3L);

		// then
		assertThat(product.getStock()).isEqualTo(8L);
	}

	@ParameterizedTest
	@MethodSource("provideValues")
	@DisplayName("상품의 재고를 증가시킬 때, 수량이 null이거나 0이하라면 IllegalArgumentException이 발생한다.")
	void increaseStockFailIfQuantityIsNullOrLeoZero(Long invalidQuantity) {
		// given
		Product product = Instancio.of(Product.class)
			.set(Select.field(Product::getStock), 5L)
			.create();

		// when & then
		assertThatThrownBy(() -> product.increaseStock(invalidQuantity))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("수량이 null이거나 0 이하입니다.");
	}

	@ParameterizedTest
	@MethodSource("provideValues")
	@DisplayName("상품의 재고를 감소시킬 때, 수량이 null이거나 0이하라면 예외가 발생한다.")
	void reduceStockFailIfQuantityIsNullOrLoeZero(Long invalidQuantity) {
		// given
		Product product = Instancio.of(Product.class)
			.set(Select.field(Product::getStock), 10L)
			.create();

		// when & then
		assertThatThrownBy(() -> product.decreaseStock(invalidQuantity))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("수량이 null이거나 0 이하입니다.");
	}

	@Test
	@DisplayName("재고보다 많은 수량을 감소시키면 ProductOutOfStockException이 발생한다.")
	void reduceStockFailIfStockAlreadyZero() {
		// given
		Product product = Instancio.of(Product.class)
			.set(Select.field(Product::getStock), 5L)
			.create();

		// when & then
		assertThatThrownBy(() -> product.decreaseStock(10L))
			.isInstanceOf(ProductOutOfStockException.class)
			.hasMessage("비즈니스 정책을 위반한 요청입니다.")
			.extracting("detail")
			.isEqualTo("재고가 부족합니다. 현재 재고 : 5, 감소 시도 수량 : 10");
	}

	private static Stream<Long> provideValues() {
		return Stream.of(0L, null, -1L);
	}

}