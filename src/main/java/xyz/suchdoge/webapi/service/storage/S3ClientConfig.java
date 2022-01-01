package xyz.suchdoge.webapi.service.storage;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3ClientConfig {
    private final AwsStorageConfig awsConfig;

    public S3ClientConfig(AwsStorageConfig awsConfig) {
        this.awsConfig = awsConfig;
    }

    @Bean
    public S3Client s3Client() {
        AwsCredentials credentials = AwsBasicCredentials.create(awsConfig.getAccessKey(), awsConfig.getSecretKey());
        return S3Client.builder()
                .credentialsProvider(() -> credentials)
                .region(Region.of(awsConfig.getRegion()))
                .build();
    }
}
