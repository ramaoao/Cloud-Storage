package com.rama.cloudstorage.dto.resource.upload;

import java.util.UUID;

public record FileUploadConfirmRequest(
        UUID objectKey
) {
}
