package com.rama.cloudstorage.service.resource;

import com.rama.cloudstorage.component.resource.ArchiveGenerationComponent;
import com.rama.cloudstorage.dto.resource.download.DownloadResponse;
import com.rama.cloudstorage.dto.resource.download.FileDownloadProjection;
import com.rama.cloudstorage.entity.Resource;
import com.rama.cloudstorage.entity.enums.ResourceStatus;
import com.rama.cloudstorage.entity.enums.ResourceType;
import com.rama.cloudstorage.exception.resource.ResourceNotFoundException;
import com.rama.cloudstorage.repository.resource.ResourceRepository;
import com.rama.cloudstorage.service.minio.MinioOperationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResourceDownloadService {

    private final ResourceRepository resourceRepository;
    private final ArchiveGenerationComponent archiveGenerationComponent;
    private final MinioOperationService minioOperationService;

    @Transactional(readOnly = true)
    public DownloadResponse download(UUID userId, UUID responseId) {
        log.info("Download started for resource {}", responseId);

        Resource target = resourceRepository.findById(responseId)
                .filter(resource -> resource.getUser().getId().equals(userId))
                .filter(resource -> resource.getStatus() == ResourceStatus.READY)
                .orElseThrow(() -> {
                    log.warn("Download failed: resource {} not found/ready", responseId);
                    return new ResourceNotFoundException("Resource not found exception.");
                });

        if (target.getType() == ResourceType.DIRECTORY) {
            log.info("Generating ZIP archive for directory '{}'", target.getName());

            List<FileDownloadProjection> files = resourceRepository.findDescendantFilesWithRelativePath(responseId);

            return new DownloadResponse(
                    target.getName() + ".zip",
                    archiveGenerationComponent.generateZipStream(files)
            );
        } else {
            return new DownloadResponse(
                    target.getName(),
                    outputStream -> {
                        try (InputStream inputStream = minioOperationService.fetch(target.getObjectKey())) {
                            inputStream.transferTo(outputStream);
                        }
                    }
            );
        }
    }
}
