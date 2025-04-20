package kr.hhplus.be.server.domain.product;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

	private final ProductRepository productRepository;

	public Product getProductById(Long productId) {
		if (productId == null) {
			throw new IllegalArgumentException("상품 ID가 null입니다.");
		}
		return productRepository.findById(productId);
	}

	public Product getProductByIdPessimistic(Long productId) {
		if (productId == null) {
			throw new IllegalArgumentException("상품 ID가 null입니다.");
		}
		return productRepository.findByIdPessimistic(productId);
	}

	public List<Product> getProductsByCondition() {
		return productRepository.findProductsByCondition();
	}

	@Transactional
	public Product decreaseStock(Long productId, Long quantity) {
		Product product = productRepository.findByIdPessimistic(productId);
		product.decreaseStock(quantity);

		return productRepository.save(product);
	}

	@Transactional
	public void restoreStock(Long productId, Long quantity) {
		Product product = productRepository.findById(productId);
		product.increaseStock(quantity);

		productRepository.save(product);
	}

}
