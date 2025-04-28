package kr.hhplus.be.server.support.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class TransactionLoggingAspect {

	@Around("@annotation(txAnn)")
	public Object logTx(ProceedingJoinPoint pjp, Transactional txAnn) throws Throwable {
		MethodSignature sig = (MethodSignature)pjp.getSignature();
		String msg = pjp.getSignature().toShortString();

		// 실제 새 트랜잭션이 시작된 경우에만 로그
		boolean isNew = TransactionAspectSupport
			.currentTransactionStatus()
			.isNewTransaction();

		if (isNew) {
			log.info("[TX-START] {} (readOnly={})", msg, txAnn.readOnly());
		}

		try {
			Object result = pjp.proceed();
			if (isNew) {
				log.info("[TX-COMMIT] {}", msg);
			}
			return result;
		} catch (Throwable ex) {
			if (isNew) {
				log.warn("[TX-ROLLBACK] {} → {}", msg, ex.toString());
			}
			throw ex;
		}
	}
}

