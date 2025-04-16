package kr.hhplus.be.server.infra.bestseller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import kr.hhplus.be.server.domain.bestseller.BestSeller;

public interface BestSellerJpaRepository extends JpaRepository<BestSeller, Long> {

	@Query(
		"SELECT b FROM BestSeller b " +
			"WHERE b.createdAt >= :from " +
			"ORDER BY b.salesCount DESC " +
			"LIMIT :limit"
	)
	List<BestSeller> findTopBySalesCountSince(LocalDateTime from, int limit);

	Optional<BestSeller> findByProductId(Long productId);
}
