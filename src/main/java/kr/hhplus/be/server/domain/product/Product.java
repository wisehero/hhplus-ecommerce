package kr.hhplus.be.server.domain.product;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import kr.hhplus.be.server.domain.base.BaseTimeEntity;
import kr.hhplus.be.server.domain.product.exception.ProductOutOfStockException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Product extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String productName;

	private String description;

	private BigDecimal price;

	private Long stock;

	@Builder
	private Product(String productName, String description, BigDecimal price, Long stock) {
		this.productName = productName;
		this.description = description;
		this.price = price;
		this.stock = stock;
	}

	public static Product create(String productName, String description, BigDecimal price, Long stock) {
		if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("가격은 0 이상이어야 합니다. 입력값: " + price);
		}

		if (stock == null || stock < 0) {
			throw new IllegalArgumentException("재고는 0 이상이어야 합니다. 입력값: " + stock);
		}

		return Product.builder()
			.productName(productName)
			.description(description)
			.price(price)
			.stock(stock)
			.build();
	}

	public void decreaseStock(Long quantity) {
		if (quantity == null || quantity <= 0) {
			throw new IllegalArgumentException("수량은 null이 아니며, 0보다 커야 합니다. 사용 시도 수량 : %d".formatted(quantity));
		}

		if (this.stock < quantity) {
			throw new ProductOutOfStockException(this.stock, quantity);
		}

		this.stock -= quantity;
	}

	public void increaseStock(Long quantity) {
		if (quantity == null || quantity <= 0) {
			throw new IllegalArgumentException("수량은 null이 아니며, 0보다 커야 합니다. 증가 시도 수량 : %d".formatted(quantity));
		}

		this.stock += quantity;
	}
}