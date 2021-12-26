package xyz.suchdoge.webapi.service.register;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import xyz.suchdoge.webapi.exception.DogeHttpException;
import xyz.suchdoge.webapi.model.token.EmailConfirmationToken;
import xyz.suchdoge.webapi.model.DogeRoleLevel;
import xyz.suchdoge.webapi.model.DogeUser;
import xyz.suchdoge.webapi.repository.DogeRoleRepository;
import xyz.suchdoge.webapi.repository.DogeUserRepository;
import xyz.suchdoge.webapi.service.DogeUserService;
import xyz.suchdoge.webapi.service.register.event.OnEmailConfirmTokenNoLongerValidEvent;
import xyz.suchdoge.webapi.service.register.event.OnEmailConfirmationNeededEvent;

@Service
public class RegisterService {
    private final DogeRoleRepository dogeRoleRepository;
    private final DogeUserRepository dogeUserRepository;
    private final DogeUserService dogeUserService;
    private final EmailConfirmationTokenService emailConfirmationTokenService;
    private final ApplicationEventPublisher eventPublisher;

    public RegisterService(DogeRoleRepository dogeRoleRepository,
                           DogeUserRepository dogeUserRepository,
                           DogeUserService dogeUserService,
                           EmailConfirmationTokenService emailConfirmationTokenService,
                           ApplicationEventPublisher eventPublisher) {
        this.dogeRoleRepository = dogeRoleRepository;
        this.dogeUserRepository = dogeUserRepository;
        this.dogeUserService = dogeUserService;
        this.emailConfirmationTokenService = emailConfirmationTokenService;
        this.eventPublisher = eventPublisher;
    }

    public DogeUser registerUser(String username, String email, String password) {
        DogeUser user = dogeUserService.createUser(username, email, password);

        this.eventPublisher.publishEvent(new OnEmailConfirmationNeededEvent(this, user));

        return user;
    }

    public DogeUser activateUser(String token) {
        final EmailConfirmationToken confirmationToken = emailConfirmationTokenService
                .getConfirmationToken(token);

        if (confirmationToken.isExpired()) {
            this.eventPublisher.publishEvent(
                    new OnEmailConfirmTokenNoLongerValidEvent(this, confirmationToken));

            throw new DogeHttpException("CONFIRM_TOKEN_EXPIRED", HttpStatus.NOT_ACCEPTABLE);
        }

        DogeUser user = confirmationToken.getUser();
        if (user.isConfirmed()) {
            this.eventPublisher.publishEvent(
                    new OnEmailConfirmTokenNoLongerValidEvent(this, confirmationToken));

            throw new DogeHttpException("DOGE_USER_ALREADY_ENABLED", HttpStatus.METHOD_NOT_ALLOWED);
        }

        user.getRoles().removeIf(dogeRole -> dogeRole.getLevel().equals(DogeRoleLevel.NOT_CONFIRMED_USER));
        user.getRoles().add(this.dogeRoleRepository.getByLevel(DogeRoleLevel.USER));
        final DogeUser enabledUser = this.dogeUserRepository.save(user);

        this.eventPublisher
                .publishEvent(new OnEmailConfirmTokenNoLongerValidEvent(this, confirmationToken));

        return enabledUser;
    }
}
