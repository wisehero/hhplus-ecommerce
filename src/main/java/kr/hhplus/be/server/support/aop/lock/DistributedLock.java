package kr.hhplus.be.server.support.aop.lock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

	String key();

	// 락을 얻기까지 대기하는 시간을 waitTime이라 한다.
	long waitTime() default 5L;

	// 락을 획득한 후 락을 유지하는 시간을 leaseTime이라 한다.
	long leaseTime() default 8L;

	TimeUnit timeUnit() default TimeUnit.SECONDS;
}
