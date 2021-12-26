package xyz.suchdoge.webapi.service.jwt;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import xyz.suchdoge.webapi.exception.DogeHttpException;
import xyz.suchdoge.webapi.model.DogeUser;
import xyz.suchdoge.webapi.model.token.RefreshToken;
import xyz.suchdoge.webapi.repository.RefreshTokenRepository;
import xyz.suchdoge.webapi.service.DogeUserService;
import xyz.suchdoge.webapi.service.HashingService;
import xyz.suchdoge.webapi.service.jwt.event.OnTooMuchRefreshTokensForUser;
import xyz.suchdoge.webapi.service.validator.ModelValidatorService;

import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final DogeUserService dogeUserService;
    private final HashingService hashingService;
    private final JwtConfig jwtConfig;
    private final JwtService jwtService;
    private final ModelValidatorService modelValidatorService;
    private final ApplicationEventPublisher eventPublisher;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                               DogeUserService dogeUserService,
                               HashingService hashingService,
                               JwtConfig jwtConfig,
                               JwtService jwtService,
                               ModelValidatorService modelValidatorService,
                               ApplicationEventPublisher eventPublisher) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.dogeUserService = dogeUserService;
        this.hashingService = hashingService;
        this.jwtConfig = jwtConfig;
        this.jwtService = jwtService;
        this.modelValidatorService = modelValidatorService;
        this.eventPublisher = eventPublisher;
    }

    public String createToken(String username) {
        final DogeUser user = this.dogeUserService.getUserByUsername(username);

        if (refreshTokenRepository.countAllByUserUsername(username) >= jwtConfig.getMaxRefreshTokensPerUser()) {
            eventPublisher.publishEvent(new OnTooMuchRefreshTokensForUser(this, user));
        }

        final String token = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .hashedToken(hashingService.hashString(token))
                .createdAt(LocalDateTime.now())
                .expirationTime(Duration.ofSeconds(jwtConfig.getRefreshTokenExpirationSeconds()))
                .user(user)
                .build();

        this.modelValidatorService.validate(refreshToken);
        this.refreshTokenRepository.save(refreshToken);

        return token;
    }

    public void createNewRefreshTokenHeader(HttpServletResponse response, String principalName) {
        String token = this.createToken(principalName);
        this.setRefreshTokenHeader(response, token);
    }

    public void setRefreshTokenHeader(HttpServletResponse response, String token) {
        final String headerValue = jwtConfig.getRefreshTokenPrefix() + token;

        response.addHeader("Access-Control-Expose-Headers", jwtConfig.getRefreshTokenHeader());
        response.setHeader(jwtConfig.getRefreshTokenHeader(), headerValue);
    }

    public RefreshToken getRefreshToken(String token) {
        return this.refreshTokenRepository.getByHashedToken(hashingService.hashString(token))
                .orElseThrow(() -> new DogeHttpException("REFRESH_TOKEN_INVALID", HttpStatus.NOT_FOUND));
    }

    public void refreshAccess(String token, HttpServletResponse response) {
        RefreshToken refreshToken = this.getRefreshToken(token);

        if (refreshToken.isExpired()) {
            throw new DogeHttpException("REFRESH_TOKEN_INVALID", HttpStatus.BAD_REQUEST);
        }

        final String username = refreshToken.getUser().getUsername();
        jwtService.createNewAuthorizationResponseHeader(response, username);

        if (refreshToken.isHalfwayExpired()) {
            this.createNewRefreshTokenHeader(response, username);
        } else {
            this.setRefreshTokenHeader(response, token);
        }
    }

    public void cleanTokens(DogeUser user) {
        Collection<RefreshToken> tokensToDelete = this.refreshTokenRepository
                .getAllByUserUsername(user.getUsername())
                .stream()
                .sorted((o1, o2) -> {
                    if (o1.isExpired() == o2.isExpired()) {
                        return o2.getCreatedAt().compareTo(o1.getCreatedAt());
                    }
                    if (o1.isExpired()) {
                        return 1;
                    }
                    return -1;
                })
                .skip(this.jwtConfig.getMaxRefreshTokensPerUser() - 1)
                .collect(Collectors.toList());

        this.refreshTokenRepository.deleteAll(tokensToDelete);
    }
}

