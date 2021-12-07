package xyz.suchdoge.webapi.service.register.listener;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import xyz.suchdoge.webapi.model.ConfirmationToken;
import xyz.suchdoge.webapi.model.DogeUser;
import xyz.suchdoge.webapi.service.EmailService;
import xyz.suchdoge.webapi.service.register.ConfirmationTokenService;
import xyz.suchdoge.webapi.service.register.event.OnEmailConfirmationNeededEvent;

@Component
public class EmailConfirmationNeededListener implements ApplicationListener<OnEmailConfirmationNeededEvent> {
    private final ConfirmationTokenService confirmationTokenService;
    private final EmailService emailService;

    public EmailConfirmationNeededListener(ConfirmationTokenService confirmationTokenService, EmailService emailService) {
        this.confirmationTokenService = confirmationTokenService;
        this.emailService = emailService;
    }

    @Override
    public void onApplicationEvent(OnEmailConfirmationNeededEvent onRegistrationCompleteEvent) {
        final DogeUser user = onRegistrationCompleteEvent.getUser();

        ConfirmationToken confirmationToken = confirmationTokenService.createToken(user);

        emailService.sendToken(user, confirmationToken);
    }
}
