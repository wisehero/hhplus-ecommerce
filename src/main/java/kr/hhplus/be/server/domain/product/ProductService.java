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

	public List<Product> getAllProducts() {
		return productRepository.findAll();
	}

	@Transactional
	public void decreaseStock(Product product, Long quantity) {
		product.decreaseStock(quantity);

		productRepository.save(product);
	}

	@Transactional
	public void restoreStock(Long productId, Long quantity) {
		Product product = productRepository.findById(productId);
		product.increaseStock(quantity);

		productRepository.save(product);
	}

}
