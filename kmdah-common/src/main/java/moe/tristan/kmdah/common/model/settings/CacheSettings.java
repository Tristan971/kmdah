package moe.tristan.kmdah.common.model.settings;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties("kmdah.cache")
public class CacheSettings {

    private final int maxSizeGb;
    private final String bucketName;
    private final S3Auth s3Auth;

    public CacheSettings(int maxSizeGb, String bucketName, S3Auth s3Auth) {
        this.maxSizeGb = maxSizeGb;
        this.bucketName = bucketName;
        this.s3Auth = s3Auth;
    }

    public int getMaxSizeGb() {
        return maxSizeGb;
    }

    public String getBucketName() {
        return bucketName;
    }

    public S3Auth getS3Auth() {
        return s3Auth;
    }

    @ConstructorBinding
    public static class S3Auth {

        private final String serviceUri;
        private final String signingRegion;
        private final String accessKeyId;
        private final String secretAccessKey;

        public S3Auth(String serviceUri, String signingRegion, String accessKeyId, String secretAccessKey) {
            this.serviceUri = serviceUri;
            this.signingRegion = signingRegion;
            this.accessKeyId = accessKeyId;
            this.secretAccessKey = secretAccessKey;
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
    }

}
