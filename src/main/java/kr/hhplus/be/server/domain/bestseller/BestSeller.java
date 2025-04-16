package kr.hhplus.be.server.domain.bestseller;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.hhplus.be.server.domain.base.BaseTimeEntity;
import kr.hhplus.be.server.domain.product.Product;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bestseller")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class BestSeller extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long productId;

	private String productName;

	private String description;

	private BigDecimal price;

	private Long stock;

	private Long salesCount;

	@Builder
	private BestSeller(Long productId, String productName, String description, BigDecimal price, Long stock,
		Long salesCount) {
		this.productId = productId;
		this.productName = productName;
		this.description = description;
		this.price = price;
		this.stock = stock;
		this.salesCount = salesCount;
	}

	public static BestSeller create(Product product, Long salesCount) {
		return new BestSeller(product.getId(), product.getProductName(), product.getDescription(), product.getPrice(),
			product.getStock(), salesCount);
	}

	public void addSalesCount(Long salesQuantity) {
		this.salesCount += salesQuantity;
	}
}
