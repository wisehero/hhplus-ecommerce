package kr.hhplus.be.server.support.aop.lock;

import static org.assertj.core.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

import kr.hhplus.be.server.support.IntgerationTestSupport;

class LockIntegrationTest extends IntgerationTestSupport {

	@Autowired
	private PubSubLockTestService pubSubLockTestService;

	@Autowired
	private SpinLockTestService spinLockTestService;

	@Autowired
	private RedissonClient redissonClient;

	@BeforeEach
	void setUp() {
		spinLockTestService.reset();
		pubSubLockTestService.reset();
	}

	@Test
	@DisplayName("PubSubLock이 정상 동작 확인 테스트 : 10개의 스레드가 동시에 1씩 증가하는 로직을 수행해도 카운터는 10이 되어야 한다.")
	void pubSubLockSuccessTest() throws InterruptedException {
		int threadCount = 10;
		ExecutorService es = Executors.newFixedThreadPool(threadCount);
		CountDownLatch latch = new CountDownLatch(threadCount);

		for (int i = 0; i < threadCount; i++) {
			es.submit(() -> {
				try {
					pubSubLockTestService.increase();
				} catch (Exception e) {
					// 예외 무시
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await();
		es.shutdown();

		assertThat(pubSubLockTestService.getCounter()).isEqualTo(10);
	}

	@Test
	@DisplayName("SpinLock 정상 동작 확인 테스트 : 10개의 스레드가 동시에 1씩 증가하는 로직을 수행해도 카운터는 10이 되어야 한다.")
	void testSpinLock() throws InterruptedException {
		int threadCount = 10;
		ExecutorService executor = Executors.newFixedThreadPool(threadCount);
		CountDownLatch latch = new CountDownLatch(threadCount);

		// 스레드에서 동시에 접근
		for (int i = 0; i < threadCount; i++) {
			executor.submit(() -> {
				try {
					spinLockTestService.increase();
				} catch (Exception e) {
					// 예외 무시
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await();
		executor.shutdown();

		// 카운터 검증
		assertThat(spinLockTestService.getCounter()).isEqualTo(10);
	}
}