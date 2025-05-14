package kr.hhplus.be.server.infra.bestseller.redis;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import kr.hhplus.be.server.domain.bestseller.dto.BestSellerItem;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BestSellerRedisRepositoryImpl implements BestSellerRedisRepository {

	private final RedisTemplate<String, Object> redisTemplate;

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
	private static final String REDIS_KEY_PREFIX_RANKING = "bestseller:realtime:";
	private static final String REDIS_KEY_PREFIX_PRODUCT_NAME = "bestseller:productname:";
	private static final int MINIMUM_BESTSELLER_COUNT = 10; // 실시간 랭킹에서 보여줄 최소 개수

	// 데이터를 유지할 일수
	private static final int DATA_RETENTION_DAYS = 2;

	@Override
	public void incrementScore(BestSellerItem item) {
		String productId = item.productId().toString();
		String rankingKey = getRealTimeRankingKey();
		String productNameKey = getRealTimeRankingProductNameKey();

		redisTemplate.opsForZSet().incrementScore(rankingKey, productId, item.quantity());
		redisTemplate.opsForHash().put(productNameKey, productId, item.productName());

		// 2일 뒤 삭제한다. 다음 날의 실시간 랭킹 10개가 부족할 수 있으므로.
		redisTemplate.expire(rankingKey, Duration.ofDays(DATA_RETENTION_DAYS));
		redisTemplate.expire(productNameKey, Duration.ofDays(DATA_RETENTION_DAYS));
	}

	@Override
	public List<String> getTodayTopProductNames(int limit) {
		//  오늘 날짜 키에서 상위 상품들 조회
		String todayKey = getRealTimeRankingKey();
		String todayProductNameKey = getRealTimeRankingProductNameKey();
		Set<Object> todayProductIds = redisTemplate.opsForZSet().reverseRange(todayKey, 0, limit - 1);

		// 오늘 데이터가 10개 이상이면 오늘 데이터 사용
		if (!CollectionUtils.isEmpty(todayProductIds) && todayProductIds.size() >= MINIMUM_BESTSELLER_COUNT) {
			List<Object> productNames = redisTemplate.opsForHash()
				.multiGet(todayProductNameKey, todayProductIds);

			return productNames.stream()
				.filter(Objects::nonNull)
				.map(Object::toString)
				.limit(limit)
				.collect(Collectors.toList());
		}

		// 2. 오늘 데이터가 10개 미만이면 어제 데이터를 그대로 사용
		String yesterdayKey = REDIS_KEY_PREFIX_RANKING +
			LocalDate.now().minusDays(1).format(DATE_FORMATTER);
		String yesterdayProductNameKey = REDIS_KEY_PREFIX_PRODUCT_NAME +
			LocalDate.now().minusDays(1).format(DATE_FORMATTER);

		Set<Object> yesterdayProductIds = redisTemplate.opsForZSet().reverseRange(yesterdayKey, 0, limit - 1);

		if (!CollectionUtils.isEmpty(yesterdayProductIds)) {
			List<Object> productNames = redisTemplate.opsForHash()
				.multiGet(yesterdayProductNameKey, yesterdayProductIds);

			return productNames.stream()
				.filter(Objects::nonNull)
				.map(Object::toString)
				.limit(limit)
				.collect(Collectors.toList());
		}

		return Collections.emptyList();
	}

	private String getRealTimeRankingKey() {
		return REDIS_KEY_PREFIX_RANKING + LocalDate.now().format(DATE_FORMATTER);
	}

	private String getRealTimeRankingProductNameKey() {
		return REDIS_KEY_PREFIX_PRODUCT_NAME + LocalDate.now().format(DATE_FORMATTER);
	}
}
