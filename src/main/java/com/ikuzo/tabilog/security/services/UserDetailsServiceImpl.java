package com.ikuzo.tabilog.security.services;

import com.ikuzo.tabilog.domain.user.User;
import com.ikuzo.tabilog.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 이메일 또는 user_id로 사용자 찾기 (JWT에서는 이메일을 사용하므로)
        User user = userRepository.findByEmail(username)
                .or(() -> userRepository.findByUserId(username))
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

        return UserDetailsImpl.build(user);
    }
}
