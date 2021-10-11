package xyz.suchdoge.webapi.service.register.listener;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import xyz.suchdoge.webapi.model.ConfirmationToken;
import xyz.suchdoge.webapi.model.DogeUser;
import xyz.suchdoge.webapi.service.EmailService;
import xyz.suchdoge.webapi.service.register.ConfirmationTokenService;
import xyz.suchdoge.webapi.service.register.event.OnRegistrationCompleteEvent;

@Component
public class RegistrationListener implements ApplicationListener<OnRegistrationCompleteEvent> {
    private final ConfirmationTokenService confirmationTokenService;
    private final EmailService emailService;

    public RegistrationListener(ConfirmationTokenService confirmationTokenService, EmailService emailService) {
        this.confirmationTokenService = confirmationTokenService;
        this.emailService = emailService;
    }

    @Override
    public void onApplicationEvent(OnRegistrationCompleteEvent onRegistrationCompleteEvent) {
        final DogeUser user = onRegistrationCompleteEvent.getUser();

        ConfirmationToken confirmationToken = confirmationTokenService.createToken(user);

        emailService.sendToken(user, confirmationToken);
    }
}
