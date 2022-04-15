package xyz.suchdoge.webapi.service.jwt.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import xyz.suchdoge.webapi.model.user.DogeUser;

@Getter
public class OnTooManyRefreshTokensForUser extends ApplicationEvent {
    private final DogeUser user;

    public OnTooManyRefreshTokensForUser(Object source, DogeUser user) {
        super(source);
        this.user = user;
    }
}
