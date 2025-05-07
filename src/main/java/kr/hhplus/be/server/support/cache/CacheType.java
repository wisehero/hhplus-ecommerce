package kr.hhplus.be.server.support.cache;

import java.time.Duration;
import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CacheType {

	BEST_SELLER_DAILY("bestSeller:daily", CacheTtlStrategy.BEST_SELLER_DAILY),
	BEST_SELLER_WEEKLY("bestSeller:weekly", CacheTtlStrategy.BEST_SELLER_WEEKLY),
	BEST_SELLER_MONTHLY("bestSeller:monthly", CacheTtlStrategy.BEST_SELLER_MONTHLY);

	private final String cacheName;
	private final CacheTtlStrategy ttlStrategy;

	public String cacheName() {
		return cacheName;
	}

	public Duration calculateTtl() {
		return ttlStrategy.calculateTtl();
	}

	// TTL은 각 기능에 맞게 다르게 작성될 수 있으므로, 아래에 추가하여 사용하면 된다.
	public interface CacheTtlStrategy {
		Duration calculateTtl();

		CacheTtlStrategy BEST_SELLER_DAILY = () -> Duration.between(
			LocalDateTime.now(),
			LocalDateTime.now().plusDays(1).toLocalDate().atTime(0, 30)
		);

		CacheTtlStrategy BEST_SELLER_WEEKLY = () -> Duration.between(
			LocalDateTime.now(),
			LocalDateTime.now().plusDays(1).toLocalDate().atTime(0, 30)
		);

		CacheTtlStrategy BEST_SELLER_MONTHLY = () -> Duration.between(
			LocalDateTime.now(),
			LocalDateTime.now().plusDays(1).toLocalDate().atTime(0, 30)
		);
	}
}
