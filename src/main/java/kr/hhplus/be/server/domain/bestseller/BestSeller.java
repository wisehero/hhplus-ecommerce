package kr.hhplus.be.server.domain.bestseller;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import kr.hhplus.be.server.domain.base.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class BestSeller extends BaseTimeEntity {

	@Id
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
}
