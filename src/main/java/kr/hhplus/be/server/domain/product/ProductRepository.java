package kr.hhplus.be.server.domain.product;

import java.util.List;

import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository {

	Product save(Product product);

	void saveAll(Product product);

	List<Product> findAll();

	Product findById(Long productId);
}
