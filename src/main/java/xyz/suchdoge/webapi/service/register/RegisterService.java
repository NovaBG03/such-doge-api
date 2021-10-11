package xyz.suchdoge.webapi.service.register;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import xyz.suchdoge.webapi.exception.DogeHttpException;
import xyz.suchdoge.webapi.model.ConfirmationToken;
import xyz.suchdoge.webapi.model.DogeUser;
import xyz.suchdoge.webapi.repository.DogeUserRepository;
import xyz.suchdoge.webapi.service.DogeUserService;
import xyz.suchdoge.webapi.service.register.event.OnConfirmTokenNoLongerValidEvent;
import xyz.suchdoge.webapi.service.register.event.OnRegistrationCompleteEvent;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RegisterService {
    private final DogeUserRepository dogeUserRepository;
    private final DogeUserService dogeUserService;
    private final ConfirmationTokenService confirmationTokenService;
    private final ApplicationEventPublisher applicationEventPublisher;

    public RegisterService(DogeUserRepository dogeUserRepository,
                           DogeUserService dogeUserService,
                           ConfirmationTokenService confirmationTokenService,
                           ApplicationEventPublisher applicationEventPublisher) {
        this.dogeUserRepository = dogeUserRepository;
        this.dogeUserService = dogeUserService;
        this.confirmationTokenService = confirmationTokenService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public DogeUser registerUser(String username, String email, String password) {
        DogeUser user = dogeUserService.createUser(username, email, password);

        this.applicationEventPublisher.publishEvent(new OnRegistrationCompleteEvent(this, user));

        return user;
    }

    public DogeUser activateUser(String token) {
        final ConfirmationToken confirmationToken = confirmationTokenService
                .getConfirmationToken(UUID.fromString(token));

        final DogeUser user = confirmationTokenService.getOwningUser(confirmationToken);

        if (user.isEnabled()) {
            this.applicationEventPublisher
                    .publishEvent(new OnConfirmTokenNoLongerValidEvent(this, confirmationToken));

            throw new DogeHttpException("DOGE_USER_ALREADY_ENABLED", HttpStatus.METHOD_NOT_ALLOWED);
        }

        user.setEnabledAt(LocalDateTime.now());
        final DogeUser enabledUser = this.dogeUserRepository.save(user);

        this.applicationEventPublisher
                .publishEvent(new OnConfirmTokenNoLongerValidEvent(this, confirmationToken));

        return enabledUser;
    }
}
