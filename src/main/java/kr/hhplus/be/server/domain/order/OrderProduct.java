package kr.hhplus.be.server.domain.order;

import java.math.BigDecimal;

import kr.hhplus.be.server.domain.product.Product;
import lombok.Getter;

@Getter
public class OrderProduct {

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
