package kr.hhplus.be.server.support.aop.lock;

import org.springframework.stereotype.Service;

@Service
public class PubSubLockTestService {
	private int counter = 0;

	@DistributedLock(key = "'testLock'")
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
