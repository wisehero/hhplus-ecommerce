package kr.hhplus.be.server;

import jakarta.annotation.PreDestroy;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import com.redis.testcontainers.RedisContainer;

@Configuration
class TestcontainersConfiguration {

	public static final MySQLContainer<?> MYSQL_CONTAINER;

	public static final RedisContainer REDIS_CONTAINER;
	private static final int REDIS_PORT = 6379;

	static {
		MYSQL_CONTAINER = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
			.withDatabaseName("hhplus")
			.withUsername("test")
			.withPassword("test");
		MYSQL_CONTAINER.start();

		System.setProperty("spring.datasource.url",
			MYSQL_CONTAINER.getJdbcUrl() + "?characterEncoding=UTF-8&serverTimezone=UTC");
		System.setProperty("spring.datasource.username", MYSQL_CONTAINER.getUsername());
		System.setProperty("spring.datasource.password", MYSQL_CONTAINER.getPassword());

		REDIS_CONTAINER = new RedisContainer(DockerImageName.parse("redis:latest"))
			.withExposedPorts(REDIS_PORT)
			.waitingFor(Wait.forListeningPort());
		REDIS_CONTAINER.start();

		System.setProperty("spring.data.redis.host", REDIS_CONTAINER.getHost());
		System.setProperty("spring.data.redis.port", String.valueOf(REDIS_CONTAINER.getMappedPort(REDIS_PORT)));

	}

	@PreDestroy
	public void preDestroy() {
		if (MYSQL_CONTAINER.isRunning()) {
			MYSQL_CONTAINER.stop();
		}

		if (REDIS_CONTAINER.isRunning()) {
			REDIS_CONTAINER.stop();
		}
	}
}