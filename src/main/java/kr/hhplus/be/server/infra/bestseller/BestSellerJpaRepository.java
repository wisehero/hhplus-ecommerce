package kr.hhplus.be.server.infra.bestseller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kr.hhplus.be.server.domain.bestseller.BestSeller;

public interface BestSellerJpaRepository extends JpaRepository<BestSeller, Long> {

	@Query(
		value = """
			SELECT b
			FROM BestSeller b
			WHERE b.updatedAt BETWEEN :startOfDay AND :endOfDay
			ORDER BY b.salesCount DESC
			LIMIT 100
			"""
	)
	List<BestSeller> findTop100DateBetween(
		@Param("startOfDay") LocalDateTime startOfDay,
		@Param("endOfDay") LocalDateTime endOfDay
	);

	Optional<BestSeller> findByProductId(Long productId);
}
