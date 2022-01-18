package xyz.suchdoge.webapi.model.token;

import lombok.*;
import xyz.suchdoge.webapi.model.user.DogeUser;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(updatable = false)
    @NotNull(message = "CONFIRMATION_TOKEN_TOKEN_NULL")
    private String hashedToken;

    @NotNull(message = "CONFIRMATION_TOKEN_CREATED_AT_NULL")
    private LocalDateTime createdAt;

    @NotNull(message = "CONFIRMATION_TOKEN_EXPIRATION_TIME_NULL")
    private Duration expirationTime;

    @ManyToOne()
    @JoinColumn(name = "user_id", nullable = false)
    private DogeUser user;

    public boolean isExpired() {
        return createdAt.plus(expirationTime).isBefore(LocalDateTime.now());
    }
}
