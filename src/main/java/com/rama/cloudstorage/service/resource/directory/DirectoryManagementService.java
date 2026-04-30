package com.rama.cloudstorage.service.resource.directory;

import com.rama.cloudstorage.component.resource.ResourceValidator;
import com.rama.cloudstorage.dto.resource.ResourceInfoDto;
import com.rama.cloudstorage.dto.resource.directory.CreateFolderRequest;
import com.rama.cloudstorage.entity.Resource;
import com.rama.cloudstorage.entity.User;
import com.rama.cloudstorage.exception.resource.directory.DirectoryAlreadyExistsException;
import com.rama.cloudstorage.mapper.ResourceMapper;
import com.rama.cloudstorage.repository.resource.ResourceRepository;
import com.rama.cloudstorage.repository.user.UserRepository;
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
public class DirectoryManagementService {

    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private final ResourceMapper resourceMapper;
    private final ResourceValidator validator;

    @Transactional(readOnly = true)
    public Slice<ResourceInfoDto> getDirectoryContent(UUID userId, UUID folderId, Pageable pageable) {
        validator.resolveFolder(userId, folderId);

        return resourceRepository.findContentsByParentId(userId, folderId, pageable)
                .map(resourceMapper::toDto);
    }

    public ResourceInfoDto createFolder(UUID userId, UUID parentId, CreateFolderRequest request) {
        User userProxy = userRepository.getReferenceById(userId);

        Resource parent = validator.resolveFolder(userId, parentId);

        try {
            Resource folder = Resource.createDirectory(userProxy, parent, request.name());
            folder = resourceRepository.saveAndFlush(folder);

            log.info("Create folder '{}' in parent directory {}", request.name(), parentId);
            return resourceMapper.toDto(folder);
        } catch (DataIntegrityViolationException e) {
            throw new DirectoryAlreadyExistsException("Folder '" + request.name() + "' already exists.");
        }
    }
}
