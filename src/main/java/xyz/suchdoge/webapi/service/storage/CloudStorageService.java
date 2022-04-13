package xyz.suchdoge.webapi.service.storage;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * Service for storing images on the clould CDN.
 *
 * @author Nikita
 */
@Service
public class CloudStorageService {
    private final S3Client s3Client;
    private final AwsStorageProps awsConfig;

    /**
     * Constructs new instance with needed dependencies.
     */
    public CloudStorageService(S3Client s3Client, AwsStorageProps awsConfig) {
        this.s3Client = s3Client;
        this.awsConfig = awsConfig;
    }

    /**
     * Upload file to the clould CDN.
     *
     * @param fileBytes file bytes to upload.
     * @param fileKey file name.
     * @param path path to file on the CDN.
     */
    public void upload(byte[] fileBytes, String fileKey, String path) {
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(awsConfig.getBucketName())
                .key(path + "/" + fileKey)
                .build();
        s3Client.putObject(putRequest, RequestBody.fromBytes(fileBytes));
    }

    /**
     * Remove file from the clould CDN.
     *
     * @param fileKey file name.
     * @param path path to file on the CDN.
     */
    public void remove(String fileKey, String path) {
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(awsConfig.getBucketName())
                .key(path + "/" + fileKey)
                .build();
        s3Client.deleteObject(deleteRequest);
    }
}
