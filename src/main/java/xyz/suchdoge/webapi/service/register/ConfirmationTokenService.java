package xyz.suchdoge.webapi.service.register;

import org.springframework.stereotype.Service;
import xyz.suchdoge.webapi.model.ConfirmationToken;
import xyz.suchdoge.webapi.model.DogeUser;
import xyz.suchdoge.webapi.repository.ConfirmationTokenRepository;
import xyz.suchdoge.webapi.service.validator.ModelValidatorService;

import java.time.Duration;
import java.time.LocalDateTime;

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

        ConfirmationToken token = ConfirmationToken.builder()
                .createdAt(LocalDateTime.now())
                .expirationTime(Duration.ofDays(registerConfig.getTokenExpirationDays()))
                .user(user)
                .build();

        modelValidatorService.validate(token);
        return confirmationTokenRepository.save(token);
    }
}
