package kr.hhplus.be.server.domain.bestseller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BestSellerRepository {

	BestSeller save(BestSeller bestSeller);

	BestSeller findById(Long bestSellerId);

	List<BestSeller> findTopBySalesCountSince(LocalDateTime from, int limit);

	void saveAll(List<BestSeller> bestSellers);

	Optional<BestSeller> findByProductId(Long productId);

}
