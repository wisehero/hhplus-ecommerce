package kr.hhplus.be.server.support;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
public abstract class E2ETestSupprot {

	@Autowired
	protected DbCleaner dbCleaner;

	@LocalServerPort
	protected int port;

	protected RestClient restClient;

	@BeforeEach
	void setUp() {
		restClient = RestClient.builder()
			.baseUrl("http://localhost:" + port)
			.build();

		dbCleaner.execute();
	}
}
