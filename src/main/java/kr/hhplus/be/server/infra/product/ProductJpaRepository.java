package kr.hhplus.be.server.infra.product;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.domain.product.Product;

public interface ProductJpaRepository extends JpaRepository<Product, Long>, ProductJpaRepositoryCustom {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT p FROM Product p WHERE p.id = :productId")
	Optional<Product> findByIdPessimistic(@Param("productId") Long productId);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
		  UPDATE Product p
		     SET p.stock = p.stock - :quantity
		   WHERE p.id = :productId
		     AND p.stock >= :quantity
		""")
	int decreaseStock(@Param("productId") Long productId, @Param("quantity") Long quantity);
}
