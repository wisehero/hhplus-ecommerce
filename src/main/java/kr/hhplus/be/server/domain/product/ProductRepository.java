package kr.hhplus.be.server.domain.product;

import java.util.List;

public interface ProductRepository {

	List<Product> findProductsByCondition();

	Product findById(Long productId);

	Product save(Product product);

	Product findByIdPessimistic(Long productId);
}
