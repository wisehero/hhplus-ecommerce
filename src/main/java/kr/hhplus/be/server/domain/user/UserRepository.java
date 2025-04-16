package kr.hhplus.be.server.domain.user;

public interface UserRepository {

	User save(User user);

	User findById(Long userId);
}
