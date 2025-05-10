package kr.hhplus.be.server.domain.bestseller.dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BestSellerResult {
	private List<BestSellerSimpleInfo> bestSellers;

	public BestSellerResult(
		List<BestSellerSimpleInfo> bestSellers
	) {
		this.bestSellers = bestSellers;
	}
}
