package xyz.suchdoge.webapi.service.jwt;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import xyz.suchdoge.webapi.exception.DogeHttpException;
import xyz.suchdoge.webapi.model.user.DogeUser;
import xyz.suchdoge.webapi.model.token.RefreshToken;
import xyz.suchdoge.webapi.repository.RefreshTokenRepository;
import xyz.suchdoge.webapi.service.DogeUserService;
import xyz.suchdoge.webapi.service.HashingService;
import xyz.suchdoge.webapi.service.jwt.event.OnTooManyRefreshTokensForUser;
import xyz.suchdoge.webapi.service.validator.ModelValidatorService;

import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing refresh tokens.
 *
 * @author Nikita
 */
@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final DogeUserService dogeUserService;
    private final HashingService hashingService;
    private final JwtService jwtService;
    private final JwtProps jwtProps;
    private final ModelValidatorService modelValidatorService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Constructs new instance with needed dependencies.
     */
    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                               DogeUserService dogeUserService,
                               HashingService hashingService,
                               JwtService jwtService,
                               JwtProps jwtConfig,
                               ModelValidatorService modelValidatorService,
                               ApplicationEventPublisher eventPublisher) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.dogeUserService = dogeUserService;
        this.hashingService = hashingService;
        this.jwtService = jwtService;
        this.jwtProps = jwtConfig;
        this.modelValidatorService = modelValidatorService;
        this.eventPublisher = eventPublisher;
    }


    /**
     * Get refresh token object from token string.
     *
     * @param token token string.
     * @return refresh token.
     * @throws DogeHttpException when refresh token invalid.
     */
    public RefreshToken getRefreshToken(String token) throws DogeHttpException {
        return this.refreshTokenRepository.getByHashedToken(hashingService.hashString(token))
                .orElseThrow(() -> new DogeHttpException("REFRESH_TOKEN_INVALID", HttpStatus.NOT_FOUND));
    }

    /**
     * Clear all old refresh tokens.
     *
     * @param user to clean tokens for.
     */
    public void clearTokens(DogeUser user) {
        Collection<RefreshToken> tokensToDelete = this.refreshTokenRepository
                .getAllByUserUsername(user.getUsername())
                .stream()
                .sorted((RefreshToken::compareTo))
                .skip(this.jwtProps.getMaxRefreshTokensPerUser() - 1)
                .collect(Collectors.toList());

        this.refreshTokenRepository.deleteAll(tokensToDelete);
    }

    /**
     * Create new refresh token for a specific user.
     *
     * @param username user to create token for.
     * @return refresh token string.
     * @throws DogeHttpException when user does not exist.
     */
    public String createToken(String username) throws DogeHttpException {
        final DogeUser user = this.dogeUserService.getUserByUsername(username);

        if (refreshTokenRepository.countAllByUserUsername(username) >= jwtProps.getMaxRefreshTokensPerUser()) {
            eventPublisher.publishEvent(new OnTooManyRefreshTokensForUser(this, user));
        }

        final String token = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .hashedToken(hashingService.hashString(token))
                .createdAt(LocalDateTime.now())
                .expirationTime(Duration.ofSeconds(jwtProps.getRefreshTokenExpirationSeconds()))
                .user(user)
                .build();

        this.modelValidatorService.validate(refreshToken);
        this.refreshTokenRepository.save(refreshToken);

        return token;
    }

    /**
     * Set new refresh token to http response.
     *
     * @param response http servlet response.
     * @param username user to create token for.
     * @throws DogeHttpException when user does not exist.
     */
    public void setRefreshTokenHeaderForUser(HttpServletResponse response, String username) throws DogeHttpException {
        String token = this.createToken(username);
        this.setRefreshTokenHeader(response, token);
    }

    /**
     * Set new jwt token to http response. Set new refresh token to http response if halfway expired.
     *
     * @param response http servlet response.
     * @param token    refresh token.
     */
    public void refreshAccess(HttpServletResponse response, String token) {
        RefreshToken refreshToken = this.getRefreshToken(token);

        if (refreshToken.isExpired()) {
            throw new DogeHttpException("REFRESH_TOKEN_INVALID", HttpStatus.BAD_REQUEST);
        }

        final String username = refreshToken.getUser().getUsername();
        jwtService.setAuthorizationResponseHeaderForUser(response, username);

        if (refreshToken.isHalfwayExpired()) {
            this.setRefreshTokenHeaderForUser(response, username);
        } else {
            this.setRefreshTokenHeader(response, token);
        }
    }

    private void setRefreshTokenHeader(HttpServletResponse response, String token) {
        final String headerValue = jwtProps.getRefreshTokenPrefix() + " " + token;

        response.addHeader("Access-Control-Expose-Headers", jwtProps.getRefreshTokenHeader());
        response.setHeader(jwtProps.getRefreshTokenHeader(), headerValue);
    }
}
