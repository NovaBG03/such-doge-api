package xyz.suchdoge.webapi.security.jwt;

import io.jsonwebtoken.security.Keys;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xyz.suchdoge.webapi.service.jwt.JwtProps;

import javax.crypto.SecretKey;

@Configuration
public class JwtSecretKey {
    private final JwtProps jwtConfig;

    public JwtSecretKey(JwtProps jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    @Bean
    public SecretKey secretKey() {
        return Keys.hmacShaKeyFor(jwtConfig.getSecretKey().getBytes());
    }
}
