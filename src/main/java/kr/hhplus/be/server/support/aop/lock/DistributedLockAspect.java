package kr.hhplus.be.server.support.aop.lock;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import kr.hhplus.be.server.support.aop.CustomSpringELParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE) // @Transactional보다 먼저 수행되어야 함을 의미한다.
@Slf4j
public class DistributedLockAspect {

	private static final String REDDISON_LOCK_KEY_PREFIX = "lock:";

	private final RedissonClient redissonClient;

	@Around("@annotation(kr.hhplus.be.server.support.aop.lock.DistributedLock)")
	public Object lock(ProceedingJoinPoint joinPoint) throws Throwable {
		MethodSignature signature = (MethodSignature)joinPoint.getSignature();
		Method method = signature.getMethod();
		DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);

		// @DistributedLock(key = "#userId")와 같이 동적으로 key를 생성할 수 있다.
		String dynamicKey = CustomSpringELParser.getDynamicValue(
			signature.getParameterNames(),
			joinPoint.getArgs(),
			distributedLock.key());
		String lockKey = REDDISON_LOCK_KEY_PREFIX + dynamicKey;
		RLock lock = redissonClient.getLock(lockKey);

		boolean isLocked = false;
		try {
			isLocked = lock.tryLock(
				distributedLock.waitTime(),
				distributedLock.leaseTime(),
				distributedLock.timeUnit()
			);

			if (!isLocked) {
				log.warn("Lock 획득 실패: {}", lockKey);
				throw new IllegalStateException("현재 작업을 수행할 수 없습니다. 다른 작업이 진행 중입니다.");
			}

			log.info("Lock 획득 성공: {}", lockKey);
			return joinPoint.proceed();

		} catch (InterruptedException e) {
			log.error("Lock 대기 중 인터럽트 발생: {}", lockKey, e);
			Thread.currentThread().interrupt();
			throw e;
		} finally {
			if (isLocked && lock.isHeldByCurrentThread()) {
				lock.unlock();
				log.info("Lock 해제: {}", lockKey);
			}
		}
	}
}
