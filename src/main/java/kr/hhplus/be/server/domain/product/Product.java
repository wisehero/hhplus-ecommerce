package kr.hhplus.be.server.domain.product;

import java.math.BigDecimal;

import kr.hhplus.be.server.domain.product.exception.ProductOutOfStockException;
import lombok.Getter;

@Getter
public class Product {

	private Long id;
	private String productName;
	private String description;
	private BigDecimal price;
	private Long stock;

	private Product(String productName, String description, BigDecimal price, Long stock) {
		this.productName = productName;
		this.description = description;
		this.price = price;
		this.stock = stock;
	}

	public static Product create(String productName, String description, BigDecimal price, Long stock) {
		if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("상품 가격이 null이거나 0 이하입니다.");
		}
		if (stock == null || stock < 0) {
			throw new IllegalArgumentException("상품 재고가 null이거나 음수입니다.");
		}

		return new Product(productName, description, price, stock);
	}

	public void decreaseStock(Long quantity) {
		if (quantity == null || quantity <= 0) {
			throw new IllegalArgumentException("수량이 null이거나 0 이하입니다.");
		}
		if (this.stock < quantity) {
			throw new ProductOutOfStockException(this.stock, quantity);
		}
		this.stock -= quantity;
	}

	public void increaseStock(Long quantity) {
		if (quantity == null || quantity <= 0) {
			throw new IllegalArgumentException("수량이 null이거나 0 이하입니다.");
		}
		this.stock += quantity;
	}
}
