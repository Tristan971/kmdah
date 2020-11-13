package moe.tristan.kmdah.common.model.settings;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties("kmdah.s3")
public class S3Settings {

    private final String serviceUri;
    private final String signingRegion;
    private final String accessKeyId;
    private final String secretAccessKey;
    private final String bucketName;

    public S3Settings(String serviceUri, String signingRegion, String accessKeyId, String secretAccessKey, String bucketName) {
        this.serviceUri = serviceUri;
        this.signingRegion = signingRegion;
        this.accessKeyId = accessKeyId;
        this.secretAccessKey = secretAccessKey;
        this.bucketName = bucketName;
    }

    public String getServiceUri() {
        return serviceUri;
    }

    public String getSigningRegion() {
        return signingRegion;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public String getSecretAccessKey() {
        return secretAccessKey;
    }

    public String getBucketName() {
        return bucketName;
    }

}
