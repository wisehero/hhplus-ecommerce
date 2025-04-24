package kr.hhplus.be.server.infra.product;

import java.util.List;

import kr.hhplus.be.server.domain.product.Product;
import kr.hhplus.be.server.interfaces.api.product.request.ProductSearchCondition;

public interface ProductJpaRepositoryCustom {
	List<Product> findProductsByCondition(ProductSearchCondition condition);
}
