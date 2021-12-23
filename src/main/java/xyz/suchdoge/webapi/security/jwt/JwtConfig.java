package xyz.suchdoge.webapi.security.jwt;

import com.google.common.net.HttpHeaders;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("application.jwt")
@Getter
@Setter
public class JwtConfig {
    private String secretKey;
    private String tokenPrefix;
    private Long tokenExpirationSeconds;

    private String refreshSecretKey;
    private String refreshTokenPrefix;
    private Long refreshTokenExpirationSeconds;
    private String refreshTokenHeader;

    public String getAuthorizationHeader() {
        return HttpHeaders.AUTHORIZATION;
    }
}
