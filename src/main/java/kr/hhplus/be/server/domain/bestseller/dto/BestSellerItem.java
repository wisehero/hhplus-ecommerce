package kr.hhplus.be.server.domain.bestseller.dto;

public record BestSellerItem(
	Long productId,
	String productName,
	Long quantity
) {
}
