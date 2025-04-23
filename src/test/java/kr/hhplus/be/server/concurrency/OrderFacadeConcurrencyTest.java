package kr.hhplus.be.server.concurrency;

import static org.assertj.core.api.Assertions.*;
import static org.instancio.Select.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.hhplus.be.server.application.order.dto.OrderCreateCommand;
import kr.hhplus.be.server.application.order.dto.OrderLine;
import kr.hhplus.be.server.application.order.facade.OrderFacade;
import kr.hhplus.be.server.domain.product.Product;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.infra.order.OrderJpaRepository;
import kr.hhplus.be.server.infra.product.ProductJpaRepository;
import kr.hhplus.be.server.infra.user.UserJpaRepository;
import kr.hhplus.be.server.support.IntgerationTestSupport;

public class OrderFacadeConcurrencyTest extends IntgerationTestSupport {

	@Autowired
	private OrderFacade orderFacade;

	@Autowired
	private ProductJpaRepository productJpaRepository;

	@Autowired
	private UserJpaRepository userJpaRepository;

	@Autowired
	private OrderJpaRepository orderJpaRepository;

	@Test
	@DisplayName("동시성 실패 테스트 : 락 없이 2명의 사용자가 재고가 1인 상품을 주문한다. 재고는 0개가 되고 주문은 2건이 생성된다.")
	void concurrentOrderFailTest() throws InterruptedException {
		// given
		User user1 = Instancio.of(User.class)
			.ignore(field("id"))
			.create();
		User user2 = Instancio.of(User.class)
			.ignore(field("id"))
			.create();
		User savedUser1 = userJpaRepository.save(user1);
		User savedUser2 = userJpaRepository.save(user2);

		Product product = Instancio.of(Product.class)
			.ignore(field("id"))
			.set(field(Product.class, "stock"), 1L)
			.create();

		Product savedProduct = productJpaRepository.save(product);

		int threadCount = 2;

		ExecutorService es = Executors.newFixedThreadPool(threadCount);
		CountDownLatch countDownLatch = new CountDownLatch(threadCount);

		// when
		es.submit(() -> {
			try {
				OrderCreateCommand command = new OrderCreateCommand(
					savedUser1.getId(),
					null,
					List.of(new OrderLine(savedProduct.getId(), 1L))
				);
				orderFacade.createOrderV1(command);
			} catch (Exception e) {
				// 예외 무시
			} finally {
				countDownLatch.countDown();
			}
		});

		es.submit(() -> {
			try {
				OrderCreateCommand command = new OrderCreateCommand(
					savedUser2.getId(),
					null,
					List.of(new OrderLine(savedProduct.getId(), 1L))
				);
				orderFacade.createOrderV1(command);
			} catch (Exception e) {
				// 예외 무시
			} finally {
				countDownLatch.countDown();
			}
		});

		countDownLatch.await();
		es.shutdown();

		// then
		Product findProduct = productJpaRepository.findById(savedProduct.getId()).orElseThrow();
		assertAll(
			() -> assertThat(findProduct.getStock()).isEqualTo(0L),
			() -> assertThat(orderJpaRepository.findAll()).hasSize(2) // 1건이 생성되어야 하는데 주문 2건 생성
		);
	}

	@Test
	@DisplayName("비관적 락 사용 : 2명의 사용자가 재고가 1인 상품을 1개씩 동시에 주문한다. 재고는 0이되고 주문은 1건만 된다.")
	void concurrentOrderTestWithPessimistic() throws InterruptedException {
		// given
		User user1 = Instancio.of(User.class)
			.ignore(field("id"))
			.create();
		User user2 = Instancio.of(User.class)
			.ignore(field("id"))
			.create();
		User savedUser1 = userJpaRepository.save(user1);
		User savedUser2 = userJpaRepository.save(user2);

		Product product = Instancio.of(Product.class)
			.ignore(field("id"))
			.set(field(Product.class, "stock"), 1L)
			.create();

		Product savedProduct = productJpaRepository.save(product);

		int threadCount = 2;

		ExecutorService es = Executors.newFixedThreadPool(threadCount);
		CountDownLatch countDownLatch = new CountDownLatch(threadCount);

		long start = System.currentTimeMillis();
		// when
		es.submit(() -> {
			try {
				OrderCreateCommand command = new OrderCreateCommand(
					savedUser1.getId(),
					null,
					List.of(new OrderLine(savedProduct.getId(), 1L))
				);
			} catch (Exception e) {
				// 예외 무시
			} finally {
				countDownLatch.countDown();
			}
		});

		es.submit(() -> {
			try {
				OrderCreateCommand command = new OrderCreateCommand(
					savedUser2.getId(),
					null,
					List.of(new OrderLine(savedProduct.getId(), 1L))
				);
				orderFacade.createOrderV2(command);
			} catch (Exception e) {
				// 예외 무시
			} finally {
				countDownLatch.countDown();
			}
		});

		countDownLatch.await();
		es.shutdown();
		long end = System.currentTimeMillis();
		System.out.println("소요시간 : " + (end - start) + "ms");
		// then
		Product findProduct = productJpaRepository.findById(savedProduct.getId()).orElseThrow();
		assertAll(
			() -> assertThat(findProduct.getStock()).isEqualTo(0L),
			() -> assertThat(orderJpaRepository.findAll()).hasSize(1) // 1건이 생성되어야 한다.
		);
	}

	@Test
	@DisplayName("@Modifying 사용 : 10명의 사용자가 재고가 10인 상품을 1개씩 동시에 주문하면 재고가 0이 된다.")
	void concurrentOrderWithModifyingForTenUsers() throws InterruptedException {
		// given
		List<User> users = IntStream.rangeClosed(1, 10)
			.mapToObj(i -> Instancio.of(User.class)
				.ignore(field("id"))
				.create())
			.toList();
		List<User> savedUsers = userJpaRepository.saveAll(users);

		Product product = Instancio.of(Product.class)
			.ignore(field("id"))
			.set(field(Product.class, "stock"), 10L)
			.create();
		Product savedProduct = productJpaRepository.save(product);

		int threadCount = 10;

		ExecutorService es = Executors.newFixedThreadPool(threadCount);
		CountDownLatch ready = new CountDownLatch(threadCount);
		CountDownLatch start = new CountDownLatch(1);

		// when
		for (User user : savedUsers) {
			es.submit(() -> {
				ready.countDown();
				try {
					start.await();
					OrderCreateCommand command = new OrderCreateCommand(
						user.getId(),
						null,
						List.of(new OrderLine(savedProduct.getId(), 1L))
					);
					orderFacade.createOrderV3(command);
				} catch (Exception e) {
					// 예외 발생해도 무시
				}
			});
		}

		ready.await();
		start.countDown();
		es.shutdown();
		es.awaitTermination(2, TimeUnit.SECONDS);

		// then
		Product findProduct = productJpaRepository.findById(savedProduct.getId()).orElseThrow();
		assertAll(
			() -> assertThat(findProduct.getStock()).isEqualTo(0L),
			() -> assertThat(orderJpaRepository.findAll()).hasSize(10) // 10건이 생성되어야 한다.
		);
	}
}
