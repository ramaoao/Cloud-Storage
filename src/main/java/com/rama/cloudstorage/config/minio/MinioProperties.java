package com.rama.cloudstorage.config.minio;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.minio")
public record MinioProperties(
    @NotBlank String endpoint,
    @NotBlank String publicEndpoint,
    @NotBlank String accessKey,
    @NotBlank String secretKey,
    @NotBlank String bucketName
) {
    public MinioProperties {
        endpoint = normalize(endpoint);
        publicEndpoint = normalize(publicEndpoint);
    }

    private static String normalize(String url) {
        return (url != null && url.endsWith("/"))
                ? url.substring(0, url.length() - 1)
                : url;
    }
}
