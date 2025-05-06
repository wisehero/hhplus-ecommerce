package kr.hhplus.be.server.support.aop.lock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SpinLock {

	String key();

	long waitTimeMillis() default 2000;

	long leaseTimeMillis() default 5000;

	long retryInterval() default 100;
}
