package kr.hhplus.be.server.infra.user;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.hhplus.be.server.domain.user.User;

public interface UserJpaRepository extends JpaRepository<User, Long> {
}
