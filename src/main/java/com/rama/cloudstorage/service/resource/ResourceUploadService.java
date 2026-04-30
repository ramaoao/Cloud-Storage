package com.rama.cloudstorage.service.resource;

import com.rama.cloudstorage.component.resource.PathNormalizer;
import com.rama.cloudstorage.component.resource.ResourceValidator;
import com.rama.cloudstorage.dto.resource.upload.FileUploadInitRequest;
import com.rama.cloudstorage.dto.resource.upload.PresignedUploadResponse;
import com.rama.cloudstorage.entity.Resource;
import com.rama.cloudstorage.entity.User;
import com.rama.cloudstorage.entity.enums.ResourceStatus;
import com.rama.cloudstorage.repository.resource.ResourceRepository;
import com.rama.cloudstorage.repository.user.UserRepository;
import com.rama.cloudstorage.service.minio.MinioOperationService;
import com.rama.cloudstorage.service.resource.tree.DirectoryTreeResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResourceUploadService {

    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private final MinioOperationService minioOperationService;
    private final ResourceValidator validator;
    private final Executor minioVirtualExecutor;
    private final DirectoryTreeResolver directoryTreeResolver;
    private final PathNormalizer pathNormalizer;

    @Retryable(
            retryFor = DataIntegrityViolationException.class,
            maxAttempts = 2,
            backoff = @Backoff(delay = 100)
    )
    @Transactional
    public List<PresignedUploadResponse> initiateUpload(UUID userId, UUID parentId, List<FileUploadInitRequest> requests) {
        User userProxy = userRepository.getReferenceById(userId);
        Resource rootParent = validator.resolveFolder(userId, parentId);

        log.info("Initiating upload for {} files in directory {}", requests.size(), parentId);

        Set<String> directoryPaths = requests.stream()
                .map(request -> pathNormalizer.extractDirectoryPath(request.relativePath()))
                .filter(path -> !path.isEmpty())
                .collect(Collectors.toSet());

        Map<String, Resource> pathCache = directoryTreeResolver.resolveAndCreateTree(userProxy, rootParent, directoryPaths);

        List<Resource> filesToSave = new ArrayList<>();
        List<PresignedUploadResponse> responses = new ArrayList<>();

        for (FileUploadInitRequest request : requests) {
            String directoryPath = pathNormalizer.extractDirectoryPath(request.relativePath());

            Resource actualParent = pathCache.getOrDefault(directoryPath, rootParent);

            Resource fileEntity = Resource.createFile(userProxy, actualParent, request.name(), request.size());
            filesToSave.add(fileEntity);

            String url = minioOperationService.generatePresignedUploadUrl(fileEntity.getObjectKey());
            responses.add(new PresignedUploadResponse(request.name(), fileEntity.getObjectKey(), url));
        }

        resourceRepository.saveAll(filesToSave);

        return responses;
    }

    public void confirmUploads(UUID userId, List<UUID> objectKeys) {
        if (objectKeys == null || objectKeys.isEmpty()) return;

        List<CompletableFuture<UUID>> futures = objectKeys.stream()
                .map(objectKey -> CompletableFuture.supplyAsync(() -> {
                    boolean exists = minioOperationService.isObjectExists(objectKey);

                    if (!exists) {
                        log.warn("Phantom upload detected. File does not exist in minio for key: {}, user: {}", objectKey, userId);
                    }

                    return exists ? objectKey : null;
                }, minioVirtualExecutor))
                .toList();

        List<UUID> validObjectKeys = futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .toList();

        if (!validObjectKeys.isEmpty()) {
            int updatedCount = resourceRepository.confirmUploads(userId, validObjectKeys, ResourceStatus.READY);
            log.info("Uploads confirmed: {}", updatedCount);
        } else {
            log.error("Upload confirmation failed: none of the {} requested objects found in minio", objectKeys.size());
        }
    }
}
