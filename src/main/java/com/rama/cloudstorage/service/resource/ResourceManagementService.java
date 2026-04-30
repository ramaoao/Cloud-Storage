package com.rama.cloudstorage.service.resource;

import com.rama.cloudstorage.component.resource.ResourceValidator;
import com.rama.cloudstorage.dto.resource.ResourceInfoDto;
import com.rama.cloudstorage.entity.Resource;
import com.rama.cloudstorage.entity.enums.ResourceType;
import com.rama.cloudstorage.exception.InvalidOperationException;
import com.rama.cloudstorage.mapper.ResourceMapper;
import com.rama.cloudstorage.repository.resource.ResourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResourceManagementService {

    private final ResourceRepository resourceRepository;
    private final ResourceMapper resourceMapper;
    private final ResourceValidator validator;

    @Transactional(readOnly = true)
    public ResourceInfoDto getResourceInfo(UUID userId, UUID resourceId) {
        Resource resource = validator.getResourceOrThrow(userId, resourceId);

        return resourceMapper.toDto(resource);
    }

    @Transactional(readOnly = true)
    public Slice<ResourceInfoDto> search(UUID userId, String query, Pageable pageable) {
        return resourceRepository.searchByName(userId, query, pageable)
                .map(resourceMapper::toDto);
    }

    @Transactional
    public ResourceInfoDto move(UUID userId, UUID resourceId, UUID targetFolderId) {
        if (resourceId.equals(targetFolderId)) {
            throw new InvalidOperationException("Cannot move resource into itself.");
        }

        Resource resource = validator.getResourceOrThrow(userId, resourceId);

        Resource targetParent = resolveAndValidateTargetParent(userId, resource, targetFolderId);

        try {
            resource.setParent(targetParent);
            resourceRepository.saveAndFlush(resource);
            log.info("User {} moved resource {} for folder {}", userId, resourceId, targetFolderId);

            return resourceMapper.toDto(resource);
        } catch (DataIntegrityViolationException e) {
            throw new InvalidOperationException("A resource with this name already exists in the target directory.");
        }
    }

    public ResourceInfoDto rename(UUID userId, UUID resourceId, String newName) {
        Resource resource = validator.getResourceOrThrow(userId, resourceId);

        try {
            resource.setName(newName);
            resourceRepository.saveAndFlush(resource);
            log.info("User {} renamed resource {} to {}", userId, resourceId, newName);

            return resourceMapper.toDto(resource);
        } catch (DataIntegrityViolationException e) {
            throw new InvalidOperationException("Name already taken in this directory.");
        }
    }

    private Resource resolveAndValidateTargetParent(UUID userId, Resource movingResource, UUID targetFolderId) {
        if (targetFolderId == null) {
            return null;
        }

        Resource targetParent = validator.resolveFolder(userId, targetFolderId);

        if (movingResource.getType() == ResourceType.DIRECTORY) {
            validator.verifyNoCyclicReference(movingResource.getId(), targetFolderId);
        }

        return targetParent;
    }
}
