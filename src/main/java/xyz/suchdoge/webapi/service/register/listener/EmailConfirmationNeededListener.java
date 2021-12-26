package xyz.suchdoge.webapi.service.register.listener;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import xyz.suchdoge.webapi.model.token.EmailConfirmationToken;
import xyz.suchdoge.webapi.model.DogeUser;
import xyz.suchdoge.webapi.service.EmailService;
import xyz.suchdoge.webapi.service.register.EmailConfirmationTokenService;
import xyz.suchdoge.webapi.service.register.event.OnEmailConfirmationNeededEvent;

@Component
public class EmailConfirmationNeededListener implements ApplicationListener<OnEmailConfirmationNeededEvent> {
    private final EmailConfirmationTokenService confirmationTokenService;
    private final EmailService emailService;

    public EmailConfirmationNeededListener(EmailConfirmationTokenService confirmationTokenService,
                                           EmailService emailService) {
        this.confirmationTokenService = confirmationTokenService;
        this.emailService = emailService;
    }

    @Override
    public void onApplicationEvent(OnEmailConfirmationNeededEvent onRegistrationCompleteEvent) {
        final DogeUser user = onRegistrationCompleteEvent.getUser();

        String confirmationToken = confirmationTokenService.createToken(user);

        emailService.sendToken(user, confirmationToken);
    }
}
