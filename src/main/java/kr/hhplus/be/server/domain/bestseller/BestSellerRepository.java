package kr.hhplus.be.server.domain.bestseller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BestSellerRepository {

	BestSeller save(BestSeller bestSeller);

	BestSeller findById(Long bestSellerId);

	void saveAll(List<BestSeller> bestSellers);

	List<BestSeller> findTop100DateBetween(LocalDateTime from, LocalDateTime to);

	Optional<BestSeller> findByProductId(Long productId);

}
