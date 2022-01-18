package xyz.suchdoge.webapi.service.register.listener;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import xyz.suchdoge.webapi.model.user.DogeUser;
import xyz.suchdoge.webapi.service.register.RegisterService;
import xyz.suchdoge.webapi.service.register.event.OnEmailConfirmationNeededEvent;

@Component
public class EmailConfirmationNeededListener implements ApplicationListener<OnEmailConfirmationNeededEvent> {
    private final RegisterService registerService;

    public EmailConfirmationNeededListener(RegisterService registerService) {
        this.registerService = registerService;
    }

    @Override
    public void onApplicationEvent(OnEmailConfirmationNeededEvent onRegistrationCompleteEvent) {
        final DogeUser user = onRegistrationCompleteEvent.getUser();
        this.registerService.sendActivationLink(user);
    }
}
