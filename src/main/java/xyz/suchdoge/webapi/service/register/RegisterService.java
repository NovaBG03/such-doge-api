package xyz.suchdoge.webapi.service.register;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import xyz.suchdoge.webapi.exception.DogeHttpException;
import xyz.suchdoge.webapi.model.token.EmailConfirmationToken;
import xyz.suchdoge.webapi.model.user.DogeRoleLevel;
import xyz.suchdoge.webapi.model.user.DogeUser;
import xyz.suchdoge.webapi.repository.DogeRoleRepository;
import xyz.suchdoge.webapi.repository.DogeUserRepository;
import xyz.suchdoge.webapi.service.DogeUserService;
import xyz.suchdoge.webapi.service.EmailService;
import xyz.suchdoge.webapi.service.register.event.OnEmailConfirmTokenNoLongerValidEvent;
import xyz.suchdoge.webapi.service.register.event.OnEmailConfirmationNeededEvent;

/**
 * Service for managing user registrations.
 *
 * @author Nikita
 */
@Service
public class RegisterService {
    private final DogeRoleRepository dogeRoleRepository;
    private final DogeUserRepository dogeUserRepository;
    private final DogeUserService dogeUserService;
    private final EmailConfirmationTokenService emailConfirmationTokenService;
    private final EmailService emailService;
    private final ApplicationEventPublisher eventPublisher;
    private final RegisterProps registerProps;

    /**
     * Constructs new instance with needed dependencies.
     */
    public RegisterService(DogeRoleRepository dogeRoleRepository,
                           DogeUserRepository dogeUserRepository,
                           DogeUserService dogeUserService,
                           EmailConfirmationTokenService emailConfirmationTokenService,
                           EmailService emailService, ApplicationEventPublisher eventPublisher,
                           RegisterProps registerConfig) {
        this.dogeRoleRepository = dogeRoleRepository;
        this.dogeUserRepository = dogeUserRepository;
        this.dogeUserService = dogeUserService;
        this.emailConfirmationTokenService = emailConfirmationTokenService;
        this.emailService = emailService;
        this.eventPublisher = eventPublisher;
        this.registerProps = registerConfig;
    }

    /**
     * Creates new user account and sends confirmation.
     *
     * @param username new account's username.
     * @param email    email associated with new account.
     * @param password password for the new account.
     * @throws DogeHttpException when can not register new user account.
     */
    public void registerUser(String username, String email, String password) throws DogeHttpException {
        DogeUser user = dogeUserService.createUser(username, email, password);
        this.eventPublisher.publishEvent(new OnEmailConfirmationNeededEvent(this, user));
    }

    /**
     * Send account activation link to user.
     *
     * @param user to receive activation link.
     */
    public void sendActivationLink(DogeUser user) {
        String confirmationToken = emailConfirmationTokenService.createToken(user);
        this.emailService.sendToken(user, confirmationToken);
    }

    /**
     * Resend account activation link to user.
     *
     * @param username to receive activation link.
     * @return minimal seconds delay between activation link requests.
     * @throws DogeHttpException when activation link is requested too soon.
     */
    public long resendActivationLink(String username) throws DogeHttpException {
        final DogeUser user = this.dogeUserService.getUserByUsername(username);
        if (this.emailConfirmationTokenService.isNewActivationTokenAvailable(user)) {
            this.sendActivationLink(user);
        }

        return registerProps.getTokenMinimalDelaySeconds();
    }

    /**
     * Activate user associated with the given activation token.
     *
     * @param token activation token.
     * @throws DogeHttpException when confirmation token is expired or user is already confirmed.
     */
    public void activateUser(String token) throws DogeHttpException {
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

        user.removeRole(DogeRoleLevel.NOT_CONFIRMED_USER);
        user.addRole(this.dogeRoleRepository.getByLevel(DogeRoleLevel.USER));
        this.dogeUserRepository.save(user);

        this.eventPublisher
                .publishEvent(new OnEmailConfirmTokenNoLongerValidEvent(this, confirmationToken));
    }
}
