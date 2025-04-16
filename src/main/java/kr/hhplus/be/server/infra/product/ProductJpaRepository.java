package kr.hhplus.be.server.infra.product;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.hhplus.be.server.domain.product.Product;

public interface ProductJpaRepository extends JpaRepository<Product, Long> {

	List<Product> findAllByIdIn(List<Long> porudctIds);
}
