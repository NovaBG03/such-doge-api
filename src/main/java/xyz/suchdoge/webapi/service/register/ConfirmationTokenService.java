package xyz.suchdoge.webapi.service.register;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import xyz.suchdoge.webapi.exception.DogeHttpException;
import xyz.suchdoge.webapi.model.ConfirmationToken;
import xyz.suchdoge.webapi.model.DogeUser;
import xyz.suchdoge.webapi.repository.ConfirmationTokenRepository;
import xyz.suchdoge.webapi.service.validator.ModelValidatorService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ConfirmationTokenService {
    private final RegisterConfig registerConfig;
    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final ModelValidatorService modelValidatorService;

    public ConfirmationTokenService(RegisterConfig registerConfig,
                                    ConfirmationTokenRepository confirmationTokenRepository,
                                    ModelValidatorService modelValidatorService) {
        this.registerConfig = registerConfig;
        this.confirmationTokenRepository = confirmationTokenRepository;
        this.modelValidatorService = modelValidatorService;
    }

    public ConfirmationToken createToken(DogeUser user) {
        if (user.isEnabled()) {
            throw new DogeHttpException("CONFIRM_TOKEN_USER_ALREADY_ENABLED", HttpStatus.BAD_REQUEST);
        }

        ConfirmationToken token = ConfirmationToken.builder()
                .createdAt(LocalDateTime.now())
                .expirationTime(Duration.ofDays(registerConfig.getTokenExpirationDays()))
                .user(user)
                .build();

        modelValidatorService.validate(token);
        return confirmationTokenRepository.save(token);
    }

    public ConfirmationToken getConfirmationToken(UUID token) {
        return this.confirmationTokenRepository.getByToken(token)
                .orElseThrow(() -> new DogeHttpException("CONFIRM_TOKEN_INVALID", HttpStatus.NOT_FOUND));
    }

    public DogeUser getOwningUser(ConfirmationToken confirmationToken) {
        if (confirmationToken.isExpired()) {
            throw new DogeHttpException("CONFIRM_TOKEN_EXPIRED", HttpStatus.NOT_ACCEPTABLE);
        }

        return confirmationToken.getUser();
    }

    public void deleteAllTokens(DogeUser user) {
        this.confirmationTokenRepository.deleteAll(user.getConfirmationTokens());
    }

    public void deleteAllExpiredTokens(DogeUser user) {
        this.confirmationTokenRepository.deleteAll(user
                .getConfirmationTokens()
                .stream()
                .filter(confirmationToken -> confirmationToken.isExpired())
                .collect(Collectors.toSet()));
    }
}
