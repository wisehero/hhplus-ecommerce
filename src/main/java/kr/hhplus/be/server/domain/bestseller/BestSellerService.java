package kr.hhplus.be.server.domain.bestseller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.server.domain.bestseller.dto.BestSellerSimpleInfo;
import kr.hhplus.be.server.domain.product.Product;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BestSellerService {

	private final BestSellerRepository bestSellerRepository;

	@Transactional
	public BestSeller save(BestSeller bestSeller) {
		return bestSellerRepository.save(bestSeller);
	}

	public List<BestSellerSimpleInfo> getTopBestSellers(LocalDateTime now, int days, int limit) {
		LocalDateTime from = now.minusDays(days);

		return bestSellerRepository.findTopBySalesCountSince(from, limit).stream()
			.map(BestSellerSimpleInfo::of).toList();
	}

	public BestSeller getProductInBestSeller(Product product) {
		return bestSellerRepository.findByProductId(product.getId())
			.orElseGet(() -> {
				// BestSeller 생성 (판매 수량 0으로 초기화)
				BestSeller newBestSeller = BestSeller.create(product, 0L);
				// 생성된 BestSeller를 DB에 저장 후 반환
				return bestSellerRepository.save(newBestSeller);
			});
	}
}
