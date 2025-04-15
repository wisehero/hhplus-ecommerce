package kr.hhplus.be.server.domain.user;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;

	public User getUserById(Long userId) {
		return userRepository.findById(userId);
	}
}
