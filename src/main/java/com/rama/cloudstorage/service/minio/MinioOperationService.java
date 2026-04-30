package com.rama.cloudstorage.service.minio;

import com.rama.cloudstorage.component.minio.MinioOperationHandler;
import com.rama.cloudstorage.config.minio.MinioProperties;
import com.rama.cloudstorage.exception.minio.StorageOperationException;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class MinioOperationService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private final MinioOperationHandler minioOperationHandler;
    private final MinioClient externalMinioClient;

    public String generatePresignedUploadUrl(UUID objectKey) {
        return minioOperationHandler.execute(() -> externalMinioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.PUT)
                        .bucket(minioProperties.bucketName())
                        .object(objectKey.toString())
                        .expiry(1, TimeUnit.HOURS)
                        .build()
        ), "Failed to generate presigned upload URL for objectKey: " + objectKey);
    }

    public InputStream fetch(UUID objectKey) {
        return minioOperationHandler.execute(() ->
                minioClient.getObject(
                        GetObjectArgs.builder()
                                .bucket(minioProperties.bucketName())
                                .object(objectKey.toString())
                                .build()
                ), "Download from MinIO failed for key: " + objectKey);
    }

    public void deleteInBatch(List<UUID> objectKey) {
        if (objectKey == null || objectKey.isEmpty()) return;
        log.info("Starting batch delete from minio: {} objects", objectKey);

        List<DeleteObject> objects = objectKey.stream()
                .map(key -> new DeleteObject(key.toString()))
                .toList();

        minioOperationHandler.execute(() -> {
            Iterable<Result<DeleteError>> results = minioClient.removeObjects(
                    RemoveObjectsArgs.builder()
                            .bucket(minioProperties.bucketName())
                            .objects(objects)
                            .build()
            );

            for (Result<DeleteError> result : results) {
                DeleteError error = result.get();
                log.error("Batch delete error for object {}: {}", error.bucketName(), error.message());
            }
            return null;
        }, "Batch delete from MinIO failed.");
    }

    public boolean isObjectExists(UUID objectKey) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minioProperties.bucketName())
                            .object(objectKey.toString())
                            .build()
            );
            return true;
        } catch (ErrorResponseException e) {
            if (isResourceNotFound(e)) {
                return false;
            }

            throw new StorageOperationException("MinIO error during statObject", e);
        } catch (Exception e) {
            throw new StorageOperationException("Unexpected storage error", e);
        }
    }

    private boolean isResourceNotFound(ErrorResponseException e) {
        String code = e.errorResponse().code();
        return "NoSuchKey".equals(code) || "NoSuchBucket".equals(code);
    }
}
