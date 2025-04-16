package kr.hhplus.be.server.integration;

import static org.assertj.core.api.Assertions.*;

import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.hhplus.be.server.support.IntgerationTestSupport;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.user.UserRepository;
import kr.hhplus.be.server.domain.user.UserService;

public class UserServiceIntgerationTest extends IntgerationTestSupport {

	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepository;

	@Test
	@DisplayName("사용자 ID로 조회하면 해당 사용자가 반환된다.")
	void shouldReturnUserWhenIdIsValid() {
		// given
		User user = Instancio.of(User.class)
			.ignore(Select.field(User.class, "id")) // ID는 DB가 생성
			.create();

		userRepository.save(user);
		Long savedId = user.getId();

		// when
		User result = userService.getUserById(savedId);

		// then
		assertThat(result.getId()).isEqualTo(savedId);
	}

}
