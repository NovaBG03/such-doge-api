package xyz.suchdoge.webapi.model;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DogeUser {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(nullable = false, updatable = false)
    private UUID id;

    @NotNull(message = "DOGE_USER_USERNAME_NULL")
    @Length.List({
            @Length(min = 3, message = "DOGE_USER_USERNAME_TOO_SHORT"),
            @Length(max = 36, message = "DOGE_USER_USERNAME_TOO_LONG")
    })
    @Column(unique = true)
    private String username;

    @NotNull(message = "DOGE_USER_EMAIL_NULL")
    @Length.List({
            @Length(min = 3, message = "DOGE_USER_EMAIL_TOO_SHORT"),
            @Length(max = 254, message = "DOGE_USER_EMAIL_TOO_LONG")
    })
    @Column(unique = true)
    private String email;

    @NotNull(message = "DOGE_USER_PASSWORD_NULL")
    @Column()
    private String encodedPassword;

    // TODO create different table for roles @JoinColumn
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private DogeUserRole role = DogeUserRole.USER;

    // TODO add validation for doge public key
    @Column()
    private String dogePublicKey;

    // TODO add validation for enabledAt
    // @FutureOrPresent
    @Column()
    private LocalDateTime enabledAt;
}
