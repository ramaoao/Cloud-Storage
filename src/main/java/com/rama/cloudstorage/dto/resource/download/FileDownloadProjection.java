package com.rama.cloudstorage.dto.resource.download;

import java.util.UUID;

public record FileDownloadProjection(
        String relativePath,
        UUID objectKey
) {
}
