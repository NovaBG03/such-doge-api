package xyz.suchdoge.webapi.service.register.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import xyz.suchdoge.webapi.model.token.EmailConfirmationToken;

@Getter
public class OnEmailConfirmTokenNoLongerValidEvent extends ApplicationEvent {
    private final EmailConfirmationToken confirmationToken;

    public OnEmailConfirmTokenNoLongerValidEvent(Object source, EmailConfirmationToken confirmationToken) {
        super(source);
        this.confirmationToken = confirmationToken;
    }
}
