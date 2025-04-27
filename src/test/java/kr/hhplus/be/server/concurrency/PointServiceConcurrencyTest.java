package kr.hhplus.be.server.concurrency;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.hhplus.be.server.domain.point.Balance;
import kr.hhplus.be.server.domain.point.Point;
import kr.hhplus.be.server.domain.point.PointRepository;
import kr.hhplus.be.server.domain.point.PointService;
import kr.hhplus.be.server.domain.point.dto.PointChargeCommand;
import kr.hhplus.be.server.domain.point.dto.PointUseCommand;
import kr.hhplus.be.server.infra.point.PointJpaRepository;
import kr.hhplus.be.server.support.IntgerationTestSupport;

public class PointServiceConcurrencyTest extends IntgerationTestSupport {

	@Autowired
	private PointService pointService;

	@Autowired
	private PointJpaRepository pointJpaRepository;

	@Test
	@DisplayName("동시성 테스트 실패 케이스: 보유 포인트 1000에서 포인트 500 충전, 포인트 100 사용 요청이 동시에 발생했을 때 정확한 잔액이 아닌 경우")
	void concurrentPointChargeAndUseFailTest() throws InterruptedException {
		// given
		Long userId = 1L;
		Balance initialBalance = Balance.createBalance(BigDecimal.valueOf(1000));
		BigDecimal chargeAmount = BigDecimal.valueOf(500);
		BigDecimal useAmount = BigDecimal.valueOf(100);

		pointJpaRepository.save(Point.create(userId, initialBalance));

		ExecutorService es = Executors.newFixedThreadPool(2);
		CountDownLatch ready = new CountDownLatch(2);
		CountDownLatch start = new CountDownLatch(1);

		Runnable chargeTask = () -> {
			ready.countDown();
			try {
				start.await();
				pointService.chargeUserPoint(new PointChargeCommand(1L, chargeAmount));
			} catch (Exception ignored) {
			}
		};

		Runnable useTask = () -> {
			ready.countDown();
			try {
				start.await();
				pointService.useUserPoint(new PointUseCommand(1L, useAmount));
			} catch (Exception ignored) {
			}
		};

		es.submit(chargeTask);
		es.submit(useTask);
		ready.await();
		start.countDown();
		es.shutdown();
		es.awaitTermination(2, TimeUnit.SECONDS);

		// then
		Point result = pointJpaRepository.findByUserId(userId).orElseThrow();
		assertThat(result.getAmount()).isNotEqualByComparingTo("1400"); // 1000 + 500 - 100 = 1400
	}

	@Test
	@DisplayName("동시성 테스트 성공 케이스(낙관적 락): 보유 포인트 1000에서 포인트 500 충전, 포인트 100 사용 요청이 동시에 발생했을 때 잔액은 1400이다.")
	void concurrentPointChargeAndUseTestOptimistic() throws InterruptedException {
		// given
		Long userId = 1L;
		Balance initialBalance = Balance.createBalance(BigDecimal.valueOf(1000));
		BigDecimal chargeAmount = BigDecimal.valueOf(500);
		BigDecimal useAmount = BigDecimal.valueOf(100);

		pointJpaRepository.save(Point.create(userId, initialBalance));

		ExecutorService es = Executors.newFixedThreadPool(2);
		CountDownLatch ready = new CountDownLatch(2);
		CountDownLatch start = new CountDownLatch(1);

		Runnable chargeTask = () -> {
			ready.countDown();
			try {
				start.await();
				pointService.chargeUserPointV2(new PointChargeCommand(1L, chargeAmount));
			} catch (Exception ignored) {
			}
		};

		Runnable useTask = () -> {
			ready.countDown();
			try {
				start.await();
				pointService.useUserPointV2(new PointUseCommand(1L, useAmount));
			} catch (Exception ignored) {
			}
		};

		es.submit(chargeTask);
		es.submit(useTask);
		ready.await();
		start.countDown();
		es.shutdown();
		es.awaitTermination(2, TimeUnit.SECONDS);

		// then
		Point result = pointJpaRepository.findByUserId(userId).orElseThrow();
		assertThat(result.getAmount()).isEqualByComparingTo("1400"); // 1000 + 500 - 100 = 1400
	}
}
