package com.rama.cloudstorage.dto.resource.upload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record FileUploadInitRequest(
        @NotBlank
        @Size(min = 1, max = 200, message = "Upload file must be between 5 and 20 characters.")
        String name,

        @Size(max = 1000, message = "Path must be between 1000 characters.")
        String relativePath,

        @PositiveOrZero
        Long size
) {
}
