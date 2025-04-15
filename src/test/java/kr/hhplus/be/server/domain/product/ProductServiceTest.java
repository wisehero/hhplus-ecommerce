package kr.hhplus.be.server.domain.product;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import kr.hhplus.be.server.domain.product.exception.ProductOutOfStockException;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

	@Mock
	private ProductRepository productRepository;

	@InjectMocks
	private ProductService productService;

	@Test
	@DisplayName("유효한 ID로 제품을 조회하면 해당 제품을 반환한다.")
	void getProductById_validId_returnsProduct() {
		// given
		Long productId = 1L;
		Product product = Instancio.of(Product.class)
			.set(Select.field(Product::getId), productId)
			.create();

		when(productRepository.findById(productId)).thenReturn(product);

		// when
		Product result = productService.getProductById(productId);

		// then
		assertThat(result).isEqualTo(product);
		verify(productRepository).findById(productId);
	}

	@Test
	@DisplayName("null ID로 제품을 조회하면 IllegalArgumentException이 발생한다.")
	void getProductById_nullId_throwsException() {
		// when & then
		assertThatThrownBy(() -> productService.getProductById(null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("상품 ID가 null입니다.");
		verify(productRepository, never()).findById(any());
	}

	@Test
	@DisplayName("재고 감소 성공 시 재고가 올바르게 감소하고 저장된다.")
	void decreaseStock_success() {
		// given
		Product product = Instancio.of(Product.class)
			.set(Select.field(Product::getStock), 10L)
			.create();

		// when
		productService.decreaseStock(product, 3L);

		// then
		assertThat(product.getStock()).isEqualTo(7L);
		verify(productRepository).save(product);
	}

	@Test
	@DisplayName("수량이 null이면 IllegalArgumentException이 발생한다.")
	void decreaseStock_nullQuantity() {
		// given
		Product product = Instancio.of(Product.class)
			.set(Select.field(Product::getStock), 10L)
			.create();

		// when & then
		assertThatThrownBy(() -> productService.decreaseStock(product, null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("수량이 null이거나 0 이하입니다.");
		verify(productRepository, never()).save(any());
	}

	@Test
	@DisplayName("재고보다 많은 수량을 감소시키면 ProductOutOfStockException이 발생한다.")
	void decreaseStock_insufficientStock() {
		// given
		Product product = Instancio.of(Product.class)
			.set(Select.field(Product::getStock), 5L)
			.create();

		// when & then
		assertThatThrownBy(() -> productService.decreaseStock(product, 10L))
			.isInstanceOf(ProductOutOfStockException.class);
		verify(productRepository, never()).save(any());
	}


	@Test
	@DisplayName("재고 복구 성공 시 재고가 올바르게 증가하고 저장된다.")
	void restoreStock_success() {
		// given
		Long productId = 1L;
		Product product = Instancio.of(Product.class)
			.set(Select.field(Product::getId), productId)
			.set(Select.field(Product::getStock), 5L)
			.create();

		when(productRepository.findById(productId)).thenReturn(product);

		// when
		productService.restoreStock(productId, 3L);

		// then
		assertThat(product.getStock()).isEqualTo(8L);
		verify(productRepository).save(product);
	}

	@Test
	@DisplayName("재고 복구시 복구할 수량이 null이면 IllegalArgumentException이 발생한다.")
	void restoreStock_nullQuantity() {
		// given
		Long productId = 1L;
		Product product = Instancio.of(Product.class)
			.set(Select.field(Product::getId), productId)
			.set(Select.field(Product::getStock), 5L)
			.create();

		when(productRepository.findById(productId)).thenReturn(product);

		// when & then
		assertThatThrownBy(() -> productService.restoreStock(productId, null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("수량이 null이거나 0 이하입니다.");
		verify(productRepository, never()).save(any());
	}

	@Test
	@DisplayName("재고 복구시 복구할 수량이 0 이하이면 IllegalArgumentException이 발생한다.")
	void restoreStock_zeroOrNegativeQuantity() {
		// given
		Long productId = 1L;
		Product product = Instancio.of(Product.class)
			.set(Select.field(Product::getId), productId)
			.set(Select.field(Product::getStock), 5L)
			.create();

		when(productRepository.findById(productId)).thenReturn(product);

		// when & then
		assertThatThrownBy(() -> productService.restoreStock(productId, 0L))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("수량이 null이거나 0 이하입니다.");
		verify(productRepository, never()).save(any());
	}
}