package kr.hhplus.be.server.domain.product;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

	private final ProductRepository productRepository;

	public List<Product> getAllProducts() {
		return productRepository.findAll();
	}

	@Transactional
	public Product decreaseStock(Long productId, Long quantity) {
		Product product = productRepository.findById(productId);
		product.decreaseStock(quantity);

		return product;
	}

	@Transactional
	public void restoreStock(Long productId, Long quantity) {
		Product product = productRepository.findById(productId);
		product.increaseStock(quantity);
	}
}
