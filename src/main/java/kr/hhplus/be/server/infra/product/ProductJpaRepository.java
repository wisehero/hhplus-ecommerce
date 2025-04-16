package kr.hhplus.be.server.infra.product;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.hhplus.be.server.domain.product.Product;

public interface ProductJpaRepository extends JpaRepository<Product, Long> {
}
