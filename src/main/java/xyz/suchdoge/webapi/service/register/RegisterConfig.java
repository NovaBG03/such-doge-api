package xyz.suchdoge.webapi.service.register;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("application.register")
@Getter
@Setter
public class RegisterConfig {
    public String tokenActivationWebUrl;
    private Integer tokenExpirationDays;
    public long tokenMinimalDelaySeconds;
}
