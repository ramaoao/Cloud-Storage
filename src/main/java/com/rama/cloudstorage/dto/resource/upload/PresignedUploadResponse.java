package com.rama.cloudstorage.dto.resource.upload;

import java.util.UUID;

public record PresignedUploadResponse(
        String name,
        UUID objectKey,
        String presignedUrl
) {
}
