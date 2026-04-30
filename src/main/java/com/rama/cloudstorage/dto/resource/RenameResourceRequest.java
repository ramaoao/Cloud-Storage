package com.rama.cloudstorage.dto.resource;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RenameResourceRequest(
        @NotBlank
        @Size(min = 1, max = 200, message = "Folder name must be between 1 and 200 characters.")
        String newName
) {
}
