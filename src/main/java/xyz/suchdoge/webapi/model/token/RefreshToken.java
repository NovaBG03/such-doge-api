package xyz.suchdoge.webapi.model.token;

import lombok.Builder;
import lombok.NoArgsConstructor;
import xyz.suchdoge.webapi.model.user.DogeUser;

import javax.persistence.Entity;
import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
public class RefreshToken extends Token {
    @Builder
    public RefreshToken(Long id, String hashedToken, LocalDateTime createdAt, Duration expirationTime, DogeUser user) {
        super(id, hashedToken, createdAt, expirationTime, user);
    }

    public boolean isHalfwayExpired() {
        return getCreatedAt().plusSeconds(getExpirationTime().getSeconds() / 2).isBefore(LocalDateTime.now())
                && !isExpired();
    }
}
