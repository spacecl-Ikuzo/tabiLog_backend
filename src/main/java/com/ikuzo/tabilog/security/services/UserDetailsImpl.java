package com.ikuzo.tabilog.security.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ikuzo.tabilog.domain.user.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

@Getter
public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;

    private final Long id;
    private final String email;
    private final String userId;
    private final String nickname;

    @JsonIgnore
    private final String password;

    // 현재는 모든 유저가 동일한 권한을 가지므로 간단하게 처리
    private final Collection<? extends GrantedAuthority> authorities = Collections.emptyList();

    public UserDetailsImpl(Long id, String email, String userId, String password, String nickname) {
        this.id = id;
        this.email = email;
        this.userId = userId;
        this.password = password;
        this.nickname = nickname;
    }

    public static UserDetailsImpl build(User user) {
        return new UserDetailsImpl(
                user.getId(),
                user.getEmail(),
                user.getUserId(),
                user.getPassword(),
                user.getNickname());
    }

    @Override
    public String getUsername() {
        return email; // Spring Security에서는 username을 식별자로 사용하므로, 우리 서비스의 email을 반환
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }
}
