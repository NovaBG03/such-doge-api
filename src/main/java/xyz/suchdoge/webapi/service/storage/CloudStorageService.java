package xyz.suchdoge.webapi.service.storage;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class CloudStorageService {
    private final S3Client s3Client;
    private final AwsStorageConfig awsConfig;

    public CloudStorageService(S3Client s3Client, AwsStorageConfig awsConfig) {
        this.s3Client = s3Client;
        this.awsConfig = awsConfig;
    }

    public void upload(byte[] fileBytes, String fileKey, String path) {
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(awsConfig.getBucketName())
                .key(path + "/" + fileKey)
                .build();
        s3Client.putObject(putRequest, RequestBody.fromBytes(fileBytes));
    }
}
