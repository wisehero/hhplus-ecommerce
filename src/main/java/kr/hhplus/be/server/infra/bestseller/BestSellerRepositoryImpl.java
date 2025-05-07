package kr.hhplus.be.server.infra.bestseller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import kr.hhplus.be.server.domain.bestseller.BestSeller;
import kr.hhplus.be.server.domain.bestseller.BestSellerRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BestSellerRepositoryImpl implements BestSellerRepository {

	private final BestSellerJpaRepository bestSellerJpaRepository;

	@Override
	public BestSeller save(BestSeller bestSeller) {
		return bestSellerJpaRepository.save(bestSeller);
	}

	@Override
	public void saveAll(List<BestSeller> bestSellers) {
		bestSellerJpaRepository.saveAll(bestSellers);
	}

	@Override
	public BestSeller findById(Long bestSellerId) {
		return bestSellerJpaRepository.findById(bestSellerId).orElseThrow(
			() -> new IllegalArgumentException("인기상품이 존재하지 않습니다.")
		);
	}

	@Override
	public List<BestSeller> findTop100DateBetween(LocalDateTime from, LocalDateTime to) {
		return bestSellerJpaRepository.findTop100DateBetween(from, to);
	}

	@Override
	public Optional<BestSeller> findByProductId(Long productId) {
		return bestSellerJpaRepository.findByProductId(productId);
	}
}
