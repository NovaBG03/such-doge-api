package xyz.suchdoge.webapi.service.jwt.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import xyz.suchdoge.webapi.model.user.DogeUser;

@Getter
public class OnTooMuchRefreshTokensForUser extends ApplicationEvent {
    private final DogeUser user;

    public OnTooMuchRefreshTokensForUser(Object source, DogeUser user) {
        super(source);
        this.user = user;
    }
}
