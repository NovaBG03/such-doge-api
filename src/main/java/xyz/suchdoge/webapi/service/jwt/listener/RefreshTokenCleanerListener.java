package xyz.suchdoge.webapi.service.jwt.listener;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import xyz.suchdoge.webapi.model.user.DogeUser;
import xyz.suchdoge.webapi.service.jwt.RefreshTokenService;
import xyz.suchdoge.webapi.service.jwt.event.OnTooManyRefreshTokensForUser;

@Component
public class RefreshTokenCleanerListener implements ApplicationListener<OnTooManyRefreshTokensForUser> {
    private final RefreshTokenService refreshTokenService;

    public RefreshTokenCleanerListener(RefreshTokenService refreshTokenService) {
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    public void onApplicationEvent(OnTooManyRefreshTokensForUser onTooMuchRefreshTokensForUser) {
        final DogeUser user = onTooMuchRefreshTokensForUser.getUser();
        refreshTokenService.clearTokens(user);
    }
}
