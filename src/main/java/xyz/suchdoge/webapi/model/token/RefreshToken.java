package xyz.suchdoge.webapi.model.token;

import lombok.Builder;
import lombok.NoArgsConstructor;
import xyz.suchdoge.webapi.model.DogeUser;

import javax.persistence.Entity;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor
public class RefreshToken extends Token {
    @Builder
    public RefreshToken(UUID token, LocalDateTime createdAt, Duration expirationTime, DogeUser user) {
        super(token, createdAt, expirationTime, user);
    }
}
