package kr.hhplus.be.server.concurrency;

import static org.assertj.core.api.Assertions.*;
import static org.instancio.Select.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.OrderService;
import kr.hhplus.be.server.domain.product.Product;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.infra.order.OrderJpaRepository;
import kr.hhplus.be.server.support.IntgerationTestSupport;

public class OrderServiceConcurrencyTest extends IntgerationTestSupport {

	@Autowired
	private OrderService orderService;

	@Autowired
	private OrderJpaRepository orderRepository;

	@Test
	@DisplayName("동시에 order()를 호출하더라도 한번만 주문이 생성된다.")
	void testConcurrentOrderDuplication() throws InterruptedException {
		// given
		User user = Instancio.of(User.class)
			.set(field(User.class, "id"), 1L)
			.create();

		Product product = Instancio.of(Product.class)
			.ignore(field(Product.class, "id"))
			.create();

		CountDownLatch ready = new CountDownLatch(2);
		CountDownLatch start = new CountDownLatch(1);
		ExecutorService exec = Executors.newFixedThreadPool(2);

		Runnable task = () -> {
			ready.countDown();
			try {
				start.await();

				Order order = Order.create(user);
				order.addOrderProduct(product, 1L);
				orderService.order(order);
			} catch (InterruptedException ignored) {
			}
		};

		// when
		exec.submit(task);
		exec.submit(task);
		ready.await();

		start.countDown();
		exec.shutdown();
		exec.awaitTermination(1, TimeUnit.SECONDS);

		// then: 한 건만 생성되길 기대했지만 실제 2건이 생성됨 → 실패 재현
		List<Order> all = orderRepository.findAll();
		assertThat(all).hasSize(1);
	}
}
