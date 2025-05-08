package kr.hhplus.be.server.support.aop.lock;

import org.springframework.stereotype.Service;

@Service
public class SpinLockTestService {
	private int counter = 0;

	@SpinLock(key = "'testLock'")
	public void increase() {
		counter++;
	}

	public int getCounter() {
		return counter;
	}

	public void reset() {
		counter = 0;
	}
}
