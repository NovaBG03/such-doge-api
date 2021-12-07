package xyz.suchdoge.webapi.service.register;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import xyz.suchdoge.webapi.exception.DogeHttpException;
import xyz.suchdoge.webapi.model.ConfirmationToken;
import xyz.suchdoge.webapi.model.DogeRole;
import xyz.suchdoge.webapi.model.DogeRoleLevel;
import xyz.suchdoge.webapi.model.DogeUser;
import xyz.suchdoge.webapi.repository.DogeRoleRepository;
import xyz.suchdoge.webapi.repository.DogeUserRepository;
import xyz.suchdoge.webapi.service.DogeUserService;
import xyz.suchdoge.webapi.service.register.event.OnConfirmTokenNoLongerValidEvent;
import xyz.suchdoge.webapi.service.register.event.OnEmailConfirmationNeededEvent;

import java.util.UUID;

@Service
public class RegisterService {
    private final DogeRoleRepository dogeRoleRepository;
    private final DogeUserRepository dogeUserRepository;
    private final DogeUserService dogeUserService;
    private final ConfirmationTokenService confirmationTokenService;
    private final ApplicationEventPublisher applicationEventPublisher;

    public RegisterService(DogeRoleRepository dogeRoleRepository,
                           DogeUserRepository dogeUserRepository,
                           DogeUserService dogeUserService,
                           ConfirmationTokenService confirmationTokenService,
                           ApplicationEventPublisher applicationEventPublisher) {
        this.dogeRoleRepository = dogeRoleRepository;
        this.dogeUserRepository = dogeUserRepository;
        this.dogeUserService = dogeUserService;
        this.confirmationTokenService = confirmationTokenService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public DogeUser registerUser(String username, String email, String password) {
        DogeUser user = dogeUserService.createUser(username, email, password);

        this.applicationEventPublisher.publishEvent(new OnEmailConfirmationNeededEvent(this, user));

        return user;
    }

    public DogeUser activateUser(String token) {
        final ConfirmationToken confirmationToken = confirmationTokenService
                .getConfirmationToken(UUID.fromString(token));

        if (confirmationToken.isExpired()) {
            this.applicationEventPublisher.publishEvent(
                    new OnConfirmTokenNoLongerValidEvent(this, confirmationToken));

            throw new DogeHttpException("CONFIRM_TOKEN_EXPIRED", HttpStatus.NOT_ACCEPTABLE);
        }

        DogeUser user = confirmationToken.getUser();
        if (user.isConfirmed()) {
            this.applicationEventPublisher.publishEvent(
                    new OnConfirmTokenNoLongerValidEvent(this, confirmationToken));

            throw new DogeHttpException("DOGE_USER_ALREADY_ENABLED", HttpStatus.METHOD_NOT_ALLOWED);
        }

        user.getRoles().removeIf(dogeRole -> dogeRole.getLevel().equals(DogeRoleLevel.NOT_CONFIRMED_USER));
        user.getRoles().add(this.dogeRoleRepository.getByLevel(DogeRoleLevel.USER));
        final DogeUser enabledUser = this.dogeUserRepository.save(user);

        this.applicationEventPublisher
                .publishEvent(new OnConfirmTokenNoLongerValidEvent(this, confirmationToken));

        return enabledUser;
    }
}
