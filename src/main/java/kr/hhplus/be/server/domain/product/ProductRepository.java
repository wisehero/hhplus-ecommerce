package kr.hhplus.be.server.domain.product;

import java.util.List;

import kr.hhplus.be.server.interfaces.api.product.request.ProductSearchCondition;

public interface ProductRepository {

	List<Product> findProductsByCondition(ProductSearchCondition condition);

	Product findById(Long productId);

	Product save(Product product);

	Product findByIdPessimistic(Long productId);

	int decreaseStock(Long productId, Long quantity);
}
