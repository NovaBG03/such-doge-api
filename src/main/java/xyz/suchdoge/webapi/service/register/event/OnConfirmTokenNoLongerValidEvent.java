package xyz.suchdoge.webapi.service.register.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import xyz.suchdoge.webapi.model.ConfirmationToken;

@Getter
public class OnConfirmTokenNoLongerValidEvent extends ApplicationEvent {
    private final ConfirmationToken confirmationToken;

    public OnConfirmTokenNoLongerValidEvent(Object source, ConfirmationToken confirmationToken) {
        super(source);
        this.confirmationToken = confirmationToken;
    }
}
