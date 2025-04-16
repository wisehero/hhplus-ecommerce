package kr.hhplus.be.server.domain.order;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.hhplus.be.server.domain.base.BaseTimeEntity;
import kr.hhplus.be.server.domain.product.Product;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_product")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class OrderProduct extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private Long orderId;
	private Long productId;
	private String productName;
	private BigDecimal unitPrice;
	private Long quantity;
	private BigDecimal subTotalPrice;

	private OrderProduct(Long orderId, Long productId, String productName, BigDecimal unitPrice,
		Long quantity,
		BigDecimal subTotalPrice) {
		this.orderId = orderId;
		this.productId = productId;
		this.productName = productName;
		this.unitPrice = unitPrice;
		this.quantity = quantity;
		this.subTotalPrice = subTotalPrice;
	}

	public static OrderProduct create(Product product, Long quantity) {
		return new OrderProduct(
			null,
			product.getId(),
			product.getProductName(),
			product.getPrice(),
			quantity,
			product.getPrice().multiply(BigDecimal.valueOf(quantity))
		);
	}

	public void assignOrderId(Long orderId) {
		this.orderId = orderId;
	}
}
