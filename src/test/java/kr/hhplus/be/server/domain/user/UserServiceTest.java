package kr.hhplus.be.server.domain.user;

import static org.assertj.core.api.Assertions.*;
import static org.instancio.Select.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@InjectMocks
	private UserService userService;

	@Mock
	private UserRepository userRepository;

	@Test
	@DisplayName("사용자 정보를 ID로 조회할 수 있다.")
	void getUserInfoSuccessfully() {
		// given
		User expectedUser = Instancio.of(User.class)
			.set(Select.field(User::getId), 1L)
			.create();


		when(userRepository.findById(1L)).thenReturn(expectedUser);

		// when
		User actualUser = userService.getUserById(1L);

		// then
		assertThat(actualUser).isEqualTo(expectedUser);
		verify(userRepository).findById(1L);
	}
}