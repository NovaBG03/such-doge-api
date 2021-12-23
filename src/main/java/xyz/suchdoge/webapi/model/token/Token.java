package xyz.suchdoge.webapi.model.token;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import xyz.suchdoge.webapi.model.DogeUser;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class Token {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(nullable = false, updatable = false)
    @Type(type = "org.hibernate.type.UUIDCharType")
    private UUID token;

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