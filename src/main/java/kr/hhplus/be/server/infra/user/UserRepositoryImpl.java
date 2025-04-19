package kr.hhplus.be.server.infra.user;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityNotFoundException;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

	private final UserJpaRepository userJpaRepository;

	@Override
	public User save(User user) {
		return userJpaRepository.save(user);
	}

	@Override
	public User findById(Long userId) {
		return userJpaRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));
	}
}
