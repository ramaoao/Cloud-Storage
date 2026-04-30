package com.rama.cloudstorage.job;

import com.rama.cloudstorage.entity.Resource;
import com.rama.cloudstorage.repository.resource.ResourceRepository;
import com.rama.cloudstorage.service.minio.MinioOperationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

@Component
@Slf4j
@RequiredArgsConstructor
public class ResourceGarbageCollectorJob {

    private final ResourceRepository resourceRepository;
    private final MinioOperationService minioOperationService;
    private static final int BATCH_SIZE = 1000;

    @Scheduled(fixedDelayString = "${app.cleanup.uploading.delay}")
    public void cleanupStaleUploads() {
        log.info("Starting cleanup of stale UPLOADING resources...");

        Instant threshold = Instant.now().minus(24, ChronoUnit.HOURS);

        int totalDeleted = processInBatches(
                pageable -> resourceRepository.findStaleUploads(threshold, pageable)
        );

        if (totalDeleted > 0) {
            log.info("Cleared {} stale upload record.", totalDeleted);
        }
    }

    @Scheduled(fixedDelayString = "${app.cleanup.deleting.delay}")
    public void cleanupDeletedResources() {
        log.info("Starting cleanup of DELETING resources...");
        Instant threshold = Instant.now().minus(1, ChronoUnit.HOURS);

        int totalDeleted = processInBatches(
                pageable -> resourceRepository.findPendingDeleting(threshold, pageable)
        );

        if (totalDeleted > 0) {
            log.info("Resources successfully deleted: {}", totalDeleted);
        }
    }

    private int processInBatches(Function<Pageable, Slice<Resource>> fetchFunction) {
        int totalProcessing = 0;
        Pageable pageable = PageRequest.of(0, BATCH_SIZE);

        while (true) {
            Slice<Resource> slice = fetchFunction.apply(pageable);
            if (slice.isEmpty()) {
                break;
            }

            boolean success = processDeletion(slice);

            if (!success) {
                log.warn("Batch processing interrupted due to an error.");
                break;
            }

            totalProcessing += slice.getNumberOfElements();
        }

        return totalProcessing;
    }

    private boolean processDeletion(Slice<Resource> resources) {
        List<UUID> s3Keys = resources.stream()
                .map(Resource::getObjectKey)
                .filter(Objects::nonNull)
                .toList();

        if (!s3Keys.isEmpty()) {
            try {
                minioOperationService.deleteInBatch(s3Keys);
            } catch (Exception e) {
                log.error("Failed batch S3 deletion.", e);
                return false;
            }
        }

        List<UUID> dbIds = resources.stream()
                .map(Resource::getId)
                .toList();
        resourceRepository.deleteByIdsInBatch(dbIds);
        return true;
    }
}
