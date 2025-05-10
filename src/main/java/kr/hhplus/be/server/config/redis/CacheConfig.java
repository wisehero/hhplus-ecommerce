package kr.hhplus.be.server.config.redis;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import kr.hhplus.be.server.support.cache.CacheType;

@Configuration
@EnableCaching
public class CacheConfig {

	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(connectionFactory);

		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
		redisTemplate.setHashKeySerializer(new StringRedisSerializer());
		redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

		redisTemplate.afterPropertiesSet();
		return redisTemplate;
	}

	@Bean
	public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
		return new StringRedisTemplate(connectionFactory);
	}

	@Bean
	public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
		RedisCacheConfiguration defaultConfig = RedisCacheConfiguration
			.defaultCacheConfig()
			// Redis에 Key를 저장할 때 String으로 직렬화(변환)해서 저장
			.serializeKeysWith(
				RedisSerializationContext.SerializationPair.fromSerializer(
					new StringRedisSerializer()))
			// GenericJackson2JsonRedisSerializer 사용 - 타입 정보 포함
			.serializeValuesWith(
				RedisSerializationContext.SerializationPair.fromSerializer(
					new GenericJackson2JsonRedisSerializer()
				)
			);

		Map<String, RedisCacheConfiguration> configMap = Arrays.stream(CacheType.values())
			.collect(Collectors.toMap(
				CacheType::cacheName,
				type -> defaultConfig.entryTtl(type.calculateTtl())
			));

		return RedisCacheManager.builder(connectionFactory)
			.cacheDefaults(defaultConfig.entryTtl(Duration.ofMinutes(30)))
			.withInitialCacheConfigurations(configMap)
			.build();
	}
}
