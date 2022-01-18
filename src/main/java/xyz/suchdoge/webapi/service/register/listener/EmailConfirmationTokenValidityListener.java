package xyz.suchdoge.webapi.service.register.listener;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import xyz.suchdoge.webapi.model.token.EmailConfirmationToken;
import xyz.suchdoge.webapi.model.user.DogeUser;
import xyz.suchdoge.webapi.service.register.EmailConfirmationTokenService;
import xyz.suchdoge.webapi.service.register.event.OnEmailConfirmTokenNoLongerValidEvent;

@Component
public class EmailConfirmationTokenValidityListener implements ApplicationListener<OnEmailConfirmTokenNoLongerValidEvent> {
    private final EmailConfirmationTokenService emailConfirmationTokenService;

    public EmailConfirmationTokenValidityListener(EmailConfirmationTokenService emailConfirmationTokenService) {
        this.emailConfirmationTokenService = emailConfirmationTokenService;
    }

    @Override
    public void onApplicationEvent(OnEmailConfirmTokenNoLongerValidEvent onConfirmTokenNoLongerValidEvent) {
        final EmailConfirmationToken confirmationToken = onConfirmTokenNoLongerValidEvent.getConfirmationToken();
        final DogeUser user = confirmationToken.getUser();

        if (user.isConfirmed()) {
            this.emailConfirmationTokenService.deleteAllTokens(user);
            return;
        }

        this.emailConfirmationTokenService.deleteAllExpiredTokens(user);
    }
}
