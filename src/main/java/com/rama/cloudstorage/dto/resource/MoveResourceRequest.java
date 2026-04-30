package com.rama.cloudstorage.dto.resource;

import java.util.UUID;

public record MoveResourceRequest(
        UUID targetFolderId
) {
}
