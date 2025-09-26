package com.ikuzo.tabilog.domain.token;

import com.ikuzo.tabilog.domain.user.User;
import com.ikuzo.tabilog.domain.user.UserRepository;
import com.ikuzo.tabilog.exception.TokenRefreshException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${tabilog.app.jwtRefreshExpirationMs}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public RefreshToken createRefreshToken(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        
        // 기존 토큰이 있다면 삭제 (deleteByUser 사용)
        refreshTokenRepository.deleteByUser(user);
        refreshTokenRepository.flush(); // 즉시 삭제 반영

        RefreshToken refreshToken = new RefreshToken(
                user,
                UUID.randomUUID().toString(),
                Instant.now().plusMillis(refreshTokenDurationMs)
        );
        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(), "Refresh token was expired. Please make a new signin request");
        }
        return token;
    }

    @Transactional
    public void deleteByUserId(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            refreshTokenRepository.deleteByUser(user);
        }
    }
}
