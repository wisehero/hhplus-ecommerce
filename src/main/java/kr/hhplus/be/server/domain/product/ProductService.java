package kr.hhplus.be.server.domain.product;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.server.domain.product.exception.ProductOutOfStockException;
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

	public List<Product> getProductsByCondition() {
		return productRepository.findProductsByCondition();
	}

	@Transactional
	public Product decreaseStockLockFree(Long productId, Long quantity) {
		Product product = productRepository.findById(productId);
		product.decreaseStock(quantity);

		return productRepository.save(product);
	}

	@Transactional
	public Product decreaseStockWithPessimistic(Long productId, Long quantity) {
		Product product = productRepository.findByIdPessimistic(productId);
		product.decreaseStock(quantity);

		return productRepository.save(product);
	}

	@Transactional
	public Product decreaseStockWithModifying(Long productId, Long quantity) {
		int updatedRow = productRepository.decreaseStock(productId, quantity);

		if (updatedRow == 0) {
			Product product = productRepository.findById(productId);
			System.out.println("야 한번 실패했따.");
			throw new ProductOutOfStockException(product.getStock(), quantity);
		}

		return productRepository.findById(productId);
	}

	@Transactional
	public void restoreStock(Long productId, Long quantity) {
		Product product = productRepository.findById(productId);
		product.increaseStock(quantity);

		productRepository.save(product);
	}

}
