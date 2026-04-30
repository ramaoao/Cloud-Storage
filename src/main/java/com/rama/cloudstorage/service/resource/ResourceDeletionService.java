package com.rama.cloudstorage.service.resource;

import com.rama.cloudstorage.exception.resource.ResourceNotFoundException;
import com.rama.cloudstorage.repository.resource.ResourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResourceDeletionService {

    private final ResourceRepository resourceRepository;

    @Transactional
    public void deleteResource(UUID userId, UUID resourceId) {
        log.info("Marking resource {} as DELETING", resourceId);

        resourceRepository.findById(resourceId)
                .filter(resource -> resource.getUser().getId().equals(userId))
                .orElseThrow(() -> {
                    log.warn("Delete failed: resource {} not found or access denied", resourceId);
                    return new ResourceNotFoundException("Resource not found.");
                });

        resourceRepository.markSubtreeAsDeleting(resourceId, userId);
    }
}
