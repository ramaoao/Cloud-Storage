package com.rama.cloudstorage.component.resource;

import com.rama.cloudstorage.entity.Resource;
import com.rama.cloudstorage.entity.enums.ResourceStatus;
import com.rama.cloudstorage.entity.enums.ResourceType;
import com.rama.cloudstorage.exception.InvalidOperationException;
import com.rama.cloudstorage.exception.resource.ResourceNotFoundException;
import com.rama.cloudstorage.repository.resource.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ResourceValidator {
    private final ResourceRepository resourceRepository;

        public Resource getResourceOrThrow(UUID userId, UUID resourceId) {
            return resourceRepository.findById(resourceId)
                    .filter(resource -> resource.getUser().getId().equals(userId))
                    .filter(resource -> resource.getStatus() == ResourceStatus.READY)
                    .orElseThrow(() -> new ResourceNotFoundException("Resource not found."));
        }

        public Resource resolveFolder(UUID userId, UUID folderId) {
            if (folderId == null) {
                return null;
            }

            Resource folder = getResourceOrThrow(userId, folderId);
            if (folder.getType() != ResourceType.DIRECTORY) {
                throw new InvalidOperationException("Target is not a directory.");
            }
            return folder;
        }

    public void verifyNoCyclicReference(UUID sourceFolderId, UUID targetFolderId) {
        List<UUID> descendantIds = resourceRepository.findAllDescantedIds(sourceFolderId);
        if (descendantIds.contains(targetFolderId)) {
            throw new InvalidOperationException("Cannot move a directory into its own subdirectory.");
        }
    }
}
