package xyz.suchdoge.webapi.service.imageGenerator;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("application.image.generator")
@Getter
@Setter
public class ImageGeneratorConfig {
    private String domain;
    private String avatarType;
    private float sizePx;
    private String colorPalette;
}
