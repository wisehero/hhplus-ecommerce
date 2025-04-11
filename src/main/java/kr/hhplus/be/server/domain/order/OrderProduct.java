package kr.hhplus.be.server.domain.order;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import kr.hhplus.be.server.domain.product.Product;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderProduct {

	@Id
	private Long id;

	private Long orderId;

	private Long productId;

	private String productName;

	private BigDecimal amount;

	private Long quantity;

	@Builder
	private OrderProduct(Long orderId, Long productId, String productName, BigDecimal amount, Long quantity) {
		this.orderId = orderId;
		this.productId = productId;
		this.productName = productName;
		this.amount = amount;
		this.quantity = quantity;
	}

	public static OrderProduct create(Product product, Long quantity) {
		if (quantity == null || quantity <= 0) {
			throw new IllegalArgumentException("주문 수량은 0보다 커야 합니다.");
		}

		BigDecimal totalAmount = product.getPrice().multiply(BigDecimal.valueOf(quantity));

		return OrderProduct.builder()
			.productId(product.getId())
			.orderId(null)
			.amount(totalAmount)
			.quantity(quantity)
			.build();
	}

	public void assignOrderId(Long orderId) {
		this.orderId = orderId;
	}
}
