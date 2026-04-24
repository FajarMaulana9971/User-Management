package com.unictive.usermanagement.configs;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppPropertiesConfig {
    private JwtProperties jwt = new JwtProperties();
    private StorageProperties storage = new StorageProperties();
    private ImageProperties image = new ImageProperties();

    @Getter
    @Setter
    public static class JwtProperties {
        private String secret;
        private long expiration;
        private long refreshExpiration;
    }

    @Getter
    @Setter
    public static class StorageProperties {
        private String type = "local";
        private LocalStorageProperties local = new LocalStorageProperties();
        private S3Properties s3 = new S3Properties();
    }

    @Getter
    @Setter
    public static class LocalStorageProperties {
        private String uploadDir = "uploads";
    }

    @Getter
    @Setter
    public static class S3Properties {
        private String bucket;
        private String region;
    }

    @Getter
    @Setter
    public static class ImageProperties {
        private long maxSizeBytes = 2097152L;
        private List<String> allowedTypes = List.of("image/jpeg", "image/png");
        private float compressionQuality = 0.8f;
        private int maxWidth = 800;
        private int maxHeight = 800;
    }
}
