package com.rama.cloudstorage.dto.resource.directory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateFolderRequest(
        @NotBlank(message = "Folder name cannot be empty.")
        @Size(min = 1, max = 200, message = "Folder name must be between 1 and 200 characters.")
        @Pattern(regexp = "^[^/\\\\:*?\"<>|]+$")
        String name
) {
}
