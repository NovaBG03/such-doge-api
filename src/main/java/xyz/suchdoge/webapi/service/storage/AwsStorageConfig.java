package xyz.suchdoge.webapi.service.storage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@ConfigurationProperties("application.aws")
@Getter
@Setter
public class AwsStorageConfig {
    private String accessKey;
    private String secretKey;
    private String region;
    private String bucketName;
}
