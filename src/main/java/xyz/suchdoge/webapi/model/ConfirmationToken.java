package xyz.suchdoge.webapi.model;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfirmationToken {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(nullable = false, updatable = false)
    private UUID token;

    @NotNull(message = "CONFIRMATION_TOKEN_CREATED_AT_NULL")
    private LocalDateTime createdAt;

    // todo @After(createdAt)
    private LocalDateTime activatedAt;

    @NotNull(message = "CONFIRMATION_TOKEN_EXPIRATION_TIME_NULL")
    private Duration expirationTime;

    @ManyToOne()
    @JoinColumn(name = "user_id", nullable = false)
    private DogeUser user;
}
