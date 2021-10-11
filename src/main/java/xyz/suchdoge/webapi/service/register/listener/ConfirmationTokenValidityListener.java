package xyz.suchdoge.webapi.service.register.listener;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import xyz.suchdoge.webapi.model.ConfirmationToken;
import xyz.suchdoge.webapi.model.DogeUser;
import xyz.suchdoge.webapi.service.register.ConfirmationTokenService;
import xyz.suchdoge.webapi.service.register.event.OnConfirmTokenNoLongerValidEvent;

@Component
public class ConfirmationTokenValidityListener implements ApplicationListener<OnConfirmTokenNoLongerValidEvent> {
    private final ConfirmationTokenService confirmationTokenService;

    public ConfirmationTokenValidityListener(ConfirmationTokenService confirmationTokenService) {
        this.confirmationTokenService = confirmationTokenService;
    }

    @Override
    public void onApplicationEvent(OnConfirmTokenNoLongerValidEvent onConfirmTokenNoLongerValidEvent) {
        final ConfirmationToken confirmationToken = onConfirmTokenNoLongerValidEvent.getConfirmationToken();
        final DogeUser user = this.confirmationTokenService.getOwningUser(confirmationToken);

        if (user.isEnabled()) {
            this.confirmationTokenService.deleteAllTokens(user);
            return;
        }

        this.confirmationTokenService.deleteAllExpiredTokens(user);
    }
}
