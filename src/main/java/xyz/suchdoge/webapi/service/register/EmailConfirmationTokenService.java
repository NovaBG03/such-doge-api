package xyz.suchdoge.webapi.service.register;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import xyz.suchdoge.webapi.exception.DogeHttpException;
import xyz.suchdoge.webapi.model.token.EmailConfirmationToken;
import xyz.suchdoge.webapi.model.DogeUser;
import xyz.suchdoge.webapi.repository.EmailConfirmationTokenRepository;
import xyz.suchdoge.webapi.service.HashingService;
import xyz.suchdoge.webapi.service.validator.ModelValidatorService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EmailConfirmationTokenService {
    private final RegisterConfig registerConfig;
    private final EmailConfirmationTokenRepository emailConfirmationTokenRepository;
    private final ModelValidatorService modelValidatorService;
    private final HashingService hashingService;

    public EmailConfirmationTokenService(RegisterConfig registerConfig,
                                         EmailConfirmationTokenRepository emailConfirmationTokenRepository,
                                         ModelValidatorService modelValidatorService,
                                         HashingService hashingService) {
        this.registerConfig = registerConfig;
        this.emailConfirmationTokenRepository = emailConfirmationTokenRepository;
        this.modelValidatorService = modelValidatorService;
        this.hashingService = hashingService;
    }

    public String createToken(DogeUser user) {
        if (user.isConfirmed()) {
            throw new DogeHttpException("USER_ALREADY_ENABLED", HttpStatus.BAD_REQUEST);
        }

        final String token = UUID.randomUUID().toString();

        EmailConfirmationToken emailConfirmationToken = EmailConfirmationToken.builder()
                .hashedToken(hashingService.hashString(token))
                .createdAt(LocalDateTime.now())
                .expirationTime(Duration.ofDays(registerConfig.getTokenExpirationDays()))
                .user(user)
                .originEmail(user.getEmail())
                .build();

        modelValidatorService.validate(emailConfirmationToken);
        emailConfirmationTokenRepository.save(emailConfirmationToken);

        return token;
    }

    public EmailConfirmationToken getConfirmationToken(String token) {
        return this.emailConfirmationTokenRepository.getByHashedToken(hashingService.hashString(token))
                .orElseThrow(() -> new DogeHttpException("CONFIRM_TOKEN_INVALID", HttpStatus.NOT_FOUND));
    }

    public void deleteAllTokens(DogeUser user) {
        this.emailConfirmationTokenRepository.deleteAll(user.getConfirmationTokens());
    }

    public void deleteAllExpiredTokens(DogeUser user) {
        this.emailConfirmationTokenRepository.deleteAll(user
                .getConfirmationTokens()
                .stream()
                .filter(EmailConfirmationToken::isExpired)
                .collect(Collectors.toSet()));
    }
}
