package com.ikuzo.tabilog.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일로 사용자를 찾는 메소드
    Optional<User> findByEmail(String email);

    // 닉네임으로 사용자를 찾는 메소드
    Optional<User> findByNickname(String nickname);

    // 이메일이 존재하는지 확인하는 메소드
    boolean existsByEmail(String email);

    // 닉네임이 존재하는지 확인하는 메소드
    boolean existsByNickname(String nickname);
}
