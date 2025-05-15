package kr.hhplus.be.server.infra.bestseller.coupon;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.hhplus.be.server.infra.coupon.redis.CouponRedisRepository;
import kr.hhplus.be.server.support.RedisTestSupport;

public class CouponRedisRepositoryTest extends RedisTestSupport {

	@Autowired
	private CouponRedisRepository couponRedisRepository;

	@Test
	@DisplayName("선착순 쿠폰 발급 대기열에 사용자를 추가할 수 있다")
	void addIfAbsent_shouldAddUserToQueue() {
		// given
		Long couponId = 1L;
		Long userId = 100L;

		// when
		boolean result = couponRedisRepository.addIfAbsent(couponId, userId);

		// then
		assertThat(result).isTrue();
		assertThat(redisTemplate.opsForZSet().size("coupon:queue:" + couponId)).isEqualTo(1);
	}

	@Test
	@DisplayName("이미 대기열에 존재하는 사용자는 다시 추가되지 않는다")
	void addIfAbsent_shouldNotAddDuplicateUser() {
		// given
		Long couponId = 1L;
		Long userId = 100L;

		// when
		boolean firstResult = couponRedisRepository.addIfAbsent(couponId, userId);
		boolean secondResult = couponRedisRepository.addIfAbsent(couponId, userId);

		// then
		assertAll(
			() -> assertThat(firstResult).isTrue(),
			() -> assertThat(secondResult).isFalse(),
			() -> assertThat(redisTemplate.opsForZSet().size("coupon:queue:" + couponId)).isEqualTo(1)
		);
	}

	@Test
	@DisplayName("여러 사용자를 대기열에 추가할 수 있다")
	void addIfAbsent_shouldAddMultipleUsers() {
		// given
		Long couponId = 1L;

		// when
		boolean result1 = couponRedisRepository.addIfAbsent(couponId, 101L);
		boolean result2 = couponRedisRepository.addIfAbsent(couponId, 102L);
		boolean result3 = couponRedisRepository.addIfAbsent(couponId, 103L);

		// then
		assertAll(
			() -> assertThat(result1).isTrue(),
			() -> assertThat(result2).isTrue(),
			() -> assertThat(result3).isTrue(),
			() -> assertThat(redisTemplate.opsForZSet().size("coupon:queue:" + couponId)).isEqualTo(3)
		);
	}

	@Test
	@DisplayName("대기열에서 배치 사이즈만큼 사용자를 가져올 수 있다")
	void getNextBatchFromQueue_shouldRetrieveUsers() {
		// given
		Long couponId = 1L;
		couponRedisRepository.addIfAbsent(couponId, 101L);
		couponRedisRepository.addIfAbsent(couponId, 102L);
		couponRedisRepository.addIfAbsent(couponId, 103L);
		couponRedisRepository.addIfAbsent(couponId, 104L);
		couponRedisRepository.addIfAbsent(couponId, 105L);

		// when
		Set<String> batch = couponRedisRepository.getNextBatchFromQueue(couponId, 3, 10);

		// then
		assertAll(
			() -> assertThat(batch).hasSize(3),
			() -> assertThat(batch).contains("101", "102", "103")
		);
	}

	@Test
	@DisplayName("재고가 배치 사이즈보다 적을 경우 재고만큼만 가져온다")
	void getNextBatchFromQueue_shouldLimitByStock() {
		// given
		Long couponId = 1L;
		couponRedisRepository.addIfAbsent(couponId, 101L);
		couponRedisRepository.addIfAbsent(couponId, 102L);
		couponRedisRepository.addIfAbsent(couponId, 103L);
		couponRedisRepository.addIfAbsent(couponId, 104L);
		couponRedisRepository.addIfAbsent(couponId, 105L);

		// when
		Set<String> batch = couponRedisRepository.getNextBatchFromQueue(couponId, 10, 2);

		// then
		assertAll(
			() -> assertThat(batch).hasSize(2),
			() -> assertThat(batch).contains("101", "102")
		);
	}

	@Test
	@DisplayName("대기열에서 사용자를 제거할 수 있다")
	void removeFromQueue_shouldRemoveUsers() {
		// given
		Long couponId = 1L;
		couponRedisRepository.addIfAbsent(couponId, 101L);
		couponRedisRepository.addIfAbsent(couponId, 102L);
		couponRedisRepository.addIfAbsent(couponId, 103L);

		// when
		Set<String> usersToRemove = Set.of("101", "102");
		couponRedisRepository.removeFromQueue(couponId, usersToRemove);

		// then
		Set<String> remainingUsers = couponRedisRepository.getNextBatchFromQueue(couponId, 10, 10);
		assertAll(
			() -> assertThat(remainingUsers).hasSize(1),
			() -> assertThat(remainingUsers).contains("103")
		);
	}

	@Test
	@DisplayName("모든 쿠폰 ID를 가져올 수 있다")
	void getAllCouponIds_shouldReturnAllCouponIds() {
		// given
		couponRedisRepository.addIfAbsent(1L, 101L);
		couponRedisRepository.addIfAbsent(2L, 102L);
		couponRedisRepository.addIfAbsent(3L, 103L);

		// when
		Set<Long> couponIds = couponRedisRepository.getAllCouponIds();

		// then
		assertAll(
			() -> assertThat(couponIds).hasSize(3),
			() -> assertThat(couponIds).contains(1L, 2L, 3L)
		);
	}

	@Test
	@DisplayName("대기열에 사용자가 없을 경우 빈 Set이 반환된다")
	void getNextBatchFromQueue_shouldReturnEmptySetWhenQueueIsEmpty() {
		// given
		Long couponId = 999L;

		// when
		Set<String> batch = couponRedisRepository.getNextBatchFromQueue(couponId, 5, 10);

		// then
		assertThat(batch).isEmpty();
	}
}
