package xyz.suchdoge.webapi.model.token;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import xyz.suchdoge.webapi.model.DogeUser;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class EmailConfirmationToken extends Token {

    @NotNull(message = "CONFIRMATION_TOKEN_ORIGIN_EMAIL_NULL")
    private String originEmail;

    @Builder
    public EmailConfirmationToken(UUID token,
                                  LocalDateTime createdAt,
                                  Duration expirationTime,
                                  DogeUser user,
                                  String originEmail) {
        super(token, createdAt, expirationTime, user);
        this.originEmail = originEmail;
    }

    @Override
    public boolean isExpired() {
        return super.isExpired() || !super.getUser().getEmail().equals(this.originEmail);
    }
}
