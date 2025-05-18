package kr.hhplus.be.server.infra.coupon.redis;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CouponRedisRepository {

	private final StringRedisTemplate redisTemplate;

	// Redis Key Prefixes
	private static final String QUEUE_PREFIX = "coupon:queue:";

	// 큐에 추가
	public boolean addIfAbsent(Long couponId, Long userId) {
		String queueKey = QUEUE_PREFIX + couponId;
		long currentTimeMillis = System.currentTimeMillis();

		// NX 옵션 사용하여 중복 발급 방지
		Boolean added = redisTemplate.opsForZSet()
			.addIfAbsent(queueKey, userId.toString(), currentTimeMillis);

		// 이미 존재하면 false 반환
		return Boolean.TRUE.equals(added);
	}

	// 큐에서 다음 BatchSize 만큼 꺼내오기
	public Set<String> getNextBatchFromQueue(Long couponId, int batchSize, int availableStock) {
		String queueKey = QUEUE_PREFIX + couponId;

		// 실제로 가져올 수 있는 사용자 수 결정
		int actualBatchSize = Math.min(batchSize, availableStock);

		Set<String> userIds = redisTemplate.opsForZSet().range(queueKey, 0, actualBatchSize - 1);
		if (userIds == null || userIds.isEmpty()) {
			return Set.of();
		}

		return userIds;
	}

	// 큐에서 제거
	public void removeFromQueue(Long couponId, Set<String> userIds) {
		String queueKey = QUEUE_PREFIX + couponId;

		if (userIds != null && !userIds.isEmpty()) {
			redisTemplate.opsForZSet().remove(queueKey, userIds.toArray(new Object[0]));
		}
	}

	public Set<Long> getAllCouponIds() {
		Set<Long> couponIds = new HashSet<>();

		// 패턴과 일치하는 모든 키를 스캔
		Set<String> keys = redisTemplate.keys(QUEUE_PREFIX + "*");

		for (String key : keys) {
			// "coupon:queue:123" 형식에서 "123" 부분 추출
			String couponIdStr = key.substring(QUEUE_PREFIX.length());
			couponIds.add(Long.parseLong(couponIdStr));
		}

		return couponIds;
	}
}
