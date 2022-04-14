package xyz.suchdoge.webapi.service.register;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import xyz.suchdoge.webapi.exception.DogeHttpException;
import xyz.suchdoge.webapi.model.token.EmailConfirmationToken;
import xyz.suchdoge.webapi.model.user.DogeUser;
import xyz.suchdoge.webapi.model.token.Token;
import xyz.suchdoge.webapi.repository.EmailConfirmationTokenRepository;
import xyz.suchdoge.webapi.service.HashingService;
import xyz.suchdoge.webapi.service.validator.ModelValidatorService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing email confirmation tokens.
 *
 * @author Nikita
 */
@Service
public class EmailConfirmationTokenService {
    private final RegisterProps registerProps;
    private final EmailConfirmationTokenRepository emailConfirmationTokenRepository;
    private final ModelValidatorService modelValidatorService;
    private final HashingService hashingService;

    /**
     * Constructs new instance with needed dependencies.
     */
    public EmailConfirmationTokenService(RegisterProps registerConfig,
                                         EmailConfirmationTokenRepository emailConfirmationTokenRepository,
                                         ModelValidatorService modelValidatorService,
                                         HashingService hashingService) {
        this.registerProps = registerConfig;
        this.emailConfirmationTokenRepository = emailConfirmationTokenRepository;
        this.modelValidatorService = modelValidatorService;
        this.hashingService = hashingService;
    }

    /**
     * Create new email confirmation token associated with a specific user.
     *
     * @param user to be created activation token for.
     * @return activation token.
     * @throws DogeHttpException when can not create confirmation token.
     */
    public String createToken(DogeUser user) throws DogeHttpException {
        if (user.isConfirmed()) {
            throw new DogeHttpException("USER_ALREADY_ENABLED", HttpStatus.BAD_REQUEST);
        }

        final String token = UUID.randomUUID().toString();

        EmailConfirmationToken emailConfirmationToken = EmailConfirmationToken.builder()
                .hashedToken(hashingService.hashString(token))
                .createdAt(LocalDateTime.now())
                .expirationTime(Duration.ofDays(registerProps.getTokenExpirationDays()))
                .user(user)
                .originEmail(user.getEmail())
                .build();

        modelValidatorService.validate(emailConfirmationToken);
        emailConfirmationTokenRepository.save(emailConfirmationToken);

        return token;
    }

    /**
     * Get email confirmation token object from token string.
     *
     * @param token token string.
     * @return email confirmation tokne.
     * @throws DogeHttpException when confirmation token is not found.
     */
    public EmailConfirmationToken getConfirmationToken(String token) throws DogeHttpException {
        return this.emailConfirmationTokenRepository.getByHashedToken(hashingService.hashString(token))
                .orElseThrow(() -> new DogeHttpException("CONFIRM_TOKEN_INVALID", HttpStatus.NOT_FOUND));
    }

    /**
     * Delete all email confirmation tokens realated to user.
     *
     * @param user to delete activation tokens.
     */
    public void deleteAllTokens(DogeUser user) {
        this.emailConfirmationTokenRepository.deleteAll(user.getEmailConfirmationTokens());
    }

    /**
     * Delete all expired email confirmation tokens related to user.
     *
     * @param user to delete activation tokens.
     */
    public void deleteAllExpiredTokens(DogeUser user) {
        this.emailConfirmationTokenRepository.deleteAll(user
                .getEmailConfirmationTokens()
                .stream()
                .filter(EmailConfirmationToken::isExpired)
                .collect(Collectors.toSet()));
    }

    /**
     * Chack activation token availability.
     *
     * @param user to check activation token.
     * @return true when can create new token.
     * @throws DogeHttpException when user is already enabled or new token is requested too soon.
     */
    public boolean isNewActivationTokenAvailable(DogeUser user) throws DogeHttpException {
        if (user.isConfirmed()) {
            throw new DogeHttpException("USER_ALREADY_ENABLED", HttpStatus.BAD_REQUEST);
        }

        final Optional<EmailConfirmationToken> optionalToken = user.getEmailConfirmationTokens()
                .stream()
                .max(Comparator.comparing(Token::getCreatedAt));

        if (optionalToken.isPresent()) {
            EmailConfirmationToken token = optionalToken.get();
            LocalDateTime nowDateTime = LocalDateTime.now();
            LocalDateTime canCreateNewTokenDateTime = token.getCreatedAt()
                    .plusSeconds(this.registerProps.getTokenMinimalDelaySeconds());

            if (nowDateTime.isBefore(canCreateNewTokenDateTime)) {
                long seconds = nowDateTime.until(canCreateNewTokenDateTime, ChronoUnit.SECONDS);
                throw new DogeHttpException("CAN_NOT_SENT_NEW_TOKEN_SECONDS_LEFT_" + seconds, HttpStatus.FORBIDDEN);
            }
        }

        return true;
    }
}
