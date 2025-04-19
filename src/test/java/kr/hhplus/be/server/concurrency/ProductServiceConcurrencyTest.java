package kr.hhplus.be.server.concurrency;

import static org.assertj.core.api.Assertions.*;
import static org.instancio.Select.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.hhplus.be.server.domain.product.Product;
import kr.hhplus.be.server.domain.product.ProductRepository;
import kr.hhplus.be.server.domain.product.ProductService;
import kr.hhplus.be.server.support.IntgerationTestSupport;

public class ProductServiceConcurrencyTest extends IntgerationTestSupport {

	@Autowired
	private ProductService productService;

	@Autowired
	private ProductRepository productRepository;

	@Test
	@DisplayName("재고가 20개인 상품을 재고 감소를 20회 요청하면 재고가 0이 된다.")
	void testDecreaseStockManyConcurrent() throws InterruptedException {
		// given
		Product product = Instancio.of(Product.class)
			.ignore(field(Product.class, "id"))
			.set(field(Product.class, "stock"), 20L)
			.create();
		product = productRepository.save(product);
		Long productId = product.getId();

		int threads = 20;
		ExecutorService exec = Executors.newFixedThreadPool(threads);
		CountDownLatch ready = new CountDownLatch(threads);
		CountDownLatch start = new CountDownLatch(1);

		// when
		Runnable task = () -> {
			ready.countDown();
			try {
				start.await();
			} catch (InterruptedException ignored) {
			}
			try {
				Product p = productRepository.findById(productId);
				productService.decreaseStock(p, 1L);
			} catch (Exception ignored) {
			}
		};

		for (int i = 0; i < threads; i++) {
			exec.submit(task);
		}

		ready.await();
		start.countDown();
		exec.shutdown();
		exec.awaitTermination(1, TimeUnit.SECONDS);

		// then: 정상이면 stock == 0 이지만, 락이 없으면 대부분 19로 남음 → 실패 재현
		Product finalP = productRepository.findById(productId);
		assertThat(finalP.getStock())
			.isEqualTo(0);
	}
}
