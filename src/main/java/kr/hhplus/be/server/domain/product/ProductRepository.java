package kr.hhplus.be.server.domain.product;

import java.util.List;

public interface ProductRepository {

	List<Product> findAll();

	List<Product> findAllByIds(List<Long> productIds);

	Product findById(Long productId);

	Product save(Product product);

}
