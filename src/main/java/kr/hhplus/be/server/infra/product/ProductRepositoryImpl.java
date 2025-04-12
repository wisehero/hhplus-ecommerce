package kr.hhplus.be.server.infra.product;

import java.util.List;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityNotFoundException;
import kr.hhplus.be.server.domain.product.Product;
import kr.hhplus.be.server.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

	private final ProductJpaRepository productJpaRepository;

	public List<Product> findAll() {
		return productJpaRepository.findAll();
	}

	@Override
	public Product findById(Long productId) {
		return productJpaRepository.findById(productId)
			.orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다. 입력한 상품 ID: " + productId));
	}

	@Override
	public Product save(Product product) {
		return productJpaRepository.save(product);
	}
}
