package xyz.suchdoge.webapi.service.register.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import xyz.suchdoge.webapi.model.DogeUser;

@Getter
public class OnRegistrationCompleteEvent extends ApplicationEvent {
    private final DogeUser user;

    public OnRegistrationCompleteEvent(Object source, DogeUser user) {
        super(source);
        this.user = user;
    }
}
