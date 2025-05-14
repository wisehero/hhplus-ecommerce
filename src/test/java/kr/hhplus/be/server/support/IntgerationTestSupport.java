package kr.hhplus.be.server.support;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

@ActiveProfiles("test")
@SpringBootTest
@Testcontainers
public abstract class IntgerationTestSupport {

	@Autowired
	private DbCleaner dbCleaner;

	@Autowired
	protected StringRedisTemplate stringRedisTemplate;

	@BeforeEach
	public void clean() {
		Set<String> keys = stringRedisTemplate.keys("*");
		if (!keys.isEmpty()) {
			stringRedisTemplate.delete(keys);
		}
		dbCleaner.execute();
	}
}
