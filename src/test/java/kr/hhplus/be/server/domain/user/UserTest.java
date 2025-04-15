package kr.hhplus.be.server.domain.user;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserTest {

	@Test
	@DisplayName("사용자를 생성할 수 있다.")
	void createUser() {
		// given
		Long userId = 1L;

		// when
		User user = User.create(userId);

		// then
		assertThat(user.getId()).isEqualTo(userId);
	}

}