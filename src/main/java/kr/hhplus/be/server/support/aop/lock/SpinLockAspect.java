package kr.hhplus.be.server.support.aop.lock;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import kr.hhplus.be.server.support.aop.CustomSpringELParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE) // @Transactional보다 먼저 수행되어야 함을 의미한다.
@Slf4j
public class SpinLockAspect {

	private final StringRedisTemplate stringRedisTemplate;

	// 아래의 LOCK_VALUE는 ThreadLocal로 관리하여 각 스레드마다 고유한 UUID를 생성한다. Redisson에서는 내부적으로 tryLock() 메서드에서 이를 관리해준다.
	private static final ThreadLocal<String> LOCK_VALUE = ThreadLocal.withInitial(() -> UUID.randomUUID().toString());

	private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
		"if redis.call('get', KEYS[1]) == ARGV[1] then " +
			"return redis.call('del', KEYS[1]) " +
			"else return 0 end", Long.class);

	@Around("@annotation(kr.hhplus.be.server.support.aop.lock.SpinLock)")
	public Object applySpinLock(ProceedingJoinPoint joinPoint) throws Throwable {
		MethodSignature signature = (MethodSignature)joinPoint.getSignature();
		Method method = signature.getMethod();
		SpinLock spinLock = method.getAnnotation(SpinLock.class);

		String key = CustomSpringELParser.getDynamicValue(
			signature.getParameterNames(),
			joinPoint.getArgs(),
			spinLock.key());
		String value = LOCK_VALUE.get();

		long startTime = System.currentTimeMillis();
		long deadline = startTime + spinLock.waitTimeMillis();

		log.info("[락 시도 시작] key={}, waitTime={}ms, leaseTime={}ms", key, spinLock.waitTimeMillis(),
			spinLock.leaseTimeMillis());

		// 현재 시간이 deadline보다 작을 때까지 반복 -> 스핀 락이 무한히 돌면서 서버에 부하를 주는 것을 방지
		while (System.currentTimeMillis() < deadline) {
			Boolean acquired = stringRedisTemplate.opsForValue()
				.setIfAbsent(key, value, Duration.ofMillis(spinLock.leaseTimeMillis()));

			if (Boolean.TRUE.equals(acquired)) {
				log.info("[락 획득 성공] 키={}, 값={}", key, value);
				try {
					// 1) 비즈니스 로직 실행
					return joinPoint.proceed();
				} finally {
					// 2) 즉시 락 해제
					releaseLock(key, value);
				}
			}

			log.info("[락 재시도] 키={}, 경과시간={}ms", key, System.currentTimeMillis() - startTime);

			try {
				Thread.sleep(spinLock.retryInterval()); // 재시도 간격 = 100ms 만큼 대기
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
				log.error("[락 중단(인터럽트)] 키={}, 경과시간={}ms", key, System.currentTimeMillis() - startTime, ie);
				break;
			}
		}
		long elapsed = System.currentTimeMillis() - startTime;
		log.warn("[락 획득 실패] 키={}, 경과시간={}ms (타임아웃 {}ms)", key, elapsed, spinLock.waitTimeMillis());
		throw new IllegalStateException("락 획득 실패: " + key);
	}

	/**
	 * 실제 락 해제 로직과 로그 기록.
	 */
	private void releaseLock(String key, String value) {
		Long deleted = stringRedisTemplate.execute(UNLOCK_SCRIPT,
			Collections.singletonList(key),
			value);
		if (deleted > 0) {
			log.info("[락 해제] 키={}, 값={}", key, value);
		} else {
			log.warn("[락 해제 스킵] 키={} (소유자 아님 또는 이미 해제됨)", key);
		}
		LOCK_VALUE.remove();
	}
}
