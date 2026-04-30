package com.rama.cloudstorage.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRequestDto(
        @NotBlank(message = "Username cannot be empty.")
        @Size(min = 5, max = 20, message = "Username must be between 5 and 20 characters.")
        @Pattern(regexp = "^[a-zA-Z0-9_.-]+$", message = "Username contains invalid characters.")
        String username,

        @NotBlank(message = "Password cannot be empty.")
        @Size(min = 5, max = 20, message = "Password must be between 5 and 20 characters.")
        @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Password contains invalid characters.")
        String password
) {
}
