package kr.hhplus.be.server.interfaces.api.product.request;

import java.math.BigDecimal;

public record ProductSearchCondition(
	String productName,
	BigDecimal minPrice,
	BigDecimal maxPrice
) {
}
