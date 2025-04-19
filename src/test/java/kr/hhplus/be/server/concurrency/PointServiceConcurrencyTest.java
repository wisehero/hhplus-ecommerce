package kr.hhplus.be.server.concurrency;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.hhplus.be.server.domain.point.Balance;
import kr.hhplus.be.server.domain.point.Point;
import kr.hhplus.be.server.domain.point.PointRepository;
import kr.hhplus.be.server.domain.point.PointService;
import kr.hhplus.be.server.domain.point.dto.PointChargeCommand;
import kr.hhplus.be.server.support.IntgerationTestSupport;

public class PointServiceConcurrencyTest extends IntgerationTestSupport {

	@Autowired
	private PointService pointService;

	@Autowired
	private PointRepository pointRepository;

	@Test
	@DisplayName("동시에 포인트 충전 요청이 2번 들어오면 누락 없이 두 번 모두 충전되어야 한다")
	void testConcurrentPointChargeRace() throws InterruptedException {
		// given
		long userId = 42L;
		BigDecimal initialBalance = BigDecimal.valueOf(100L);
		BigDecimal chargeAmount = BigDecimal.valueOf(100L);

		// Instancio 로 초기 Point 엔티티 생성·저장
		Point initial = Instancio.of(Point.class)
			.ignore(Select.field(Point.class, "id"))
			.set(Select.field(Point.class, "userId"), userId)
			.set(Select.field(Balance.class, "amount"), initialBalance)
			.create();
		pointRepository.save(initial);

		// 2개의 스레드를 동시에 실행하기 위한 latch
		int threads = 2;
		ExecutorService exec = Executors.newFixedThreadPool(threads);
		CountDownLatch ready = new CountDownLatch(threads);
		CountDownLatch start = new CountDownLatch(1);

		// when: 두 스레드가 거의 동시에 chargeUserPoint 호출
		Runnable task = () -> {
			ready.countDown();
			try {
				start.await();
				pointService.chargeUserPoint(new PointChargeCommand(userId, chargeAmount));
			} catch (InterruptedException ignored) {
			}
		};

		for (int i = 0; i < threads; i++) {
			exec.submit(task);
		}
		// 모든 스레드가 준비됐을 때
		ready.await();
		// 동시에 시작 신호
		start.countDown();
		exec.shutdown();
		exec.awaitTermination(1, TimeUnit.SECONDS);

		// then: 기대는 initialBalance + 2 * chargeAmount
		Point updated = pointRepository.findByUserId(userId);
		assertAll(
			() -> assertThat(updated.getAmount())
				.as("동시 2회 충전 후 잔액이 누락 없이 두 번 모두 더해져야 한다")
				.isEqualByComparingTo(initialBalance.add(chargeAmount.multiply(BigDecimal.valueOf(2))))
		);
	}
}
