package kr.hhplus.be.server.support;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
public abstract class RedisTestSupport {

	@Autowired
	protected RedisTemplate<String, Object> redisTemplate;

	@Autowired
	protected StringRedisTemplate stringRedisTemplate;

	@BeforeEach
	void setUp() {
		// 모든 Redis 키 삭제
		Set<String> keys = stringRedisTemplate.keys("*");
		if (!keys.isEmpty()) {
			stringRedisTemplate.delete(keys);
		}
	}
}
