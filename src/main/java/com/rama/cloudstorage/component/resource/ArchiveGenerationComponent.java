package com.rama.cloudstorage.component.resource;

import com.rama.cloudstorage.dto.resource.download.FileDownloadProjection;
import com.rama.cloudstorage.exception.minio.StorageOperationException;
import com.rama.cloudstorage.service.minio.MinioOperationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
@Slf4j
@RequiredArgsConstructor
public class ArchiveGenerationComponent {

    private final MinioOperationService minioOperationService;

    public StreamingResponseBody generateZipStream(List<FileDownloadProjection> files) {
        return outputStream -> {
            try (BufferedOutputStream bufferedOut = new BufferedOutputStream(outputStream);
                ZipOutputStream zipOut = new ZipOutputStream(bufferedOut)) {
                if (files == null || files.isEmpty()) {
                    zipOut.putNextEntry(new ZipEntry("empty_folder/"));
                    zipOut.closeEntry();
                    return;
                }

                for (FileDownloadProjection file : files) {
                    processFile(zipOut, file);
                }
            }
        };
    }

    private void processFile(ZipOutputStream zipOut, FileDownloadProjection file) throws IOException {
        try {
            zipOut.putNextEntry(new ZipEntry(file.relativePath()));

            try (InputStream s3Stream = minioOperationService.fetch(file.objectKey())) {
                s3Stream.transferTo(zipOut);

            } catch (StorageOperationException e) {
                log.error("MinIO error: Failed to fetch file {} for zip archive. Skipping.", file.objectKey(), e);

            }

        } catch (IOException e) {
            log.warn("Network error or client aborted download. Reason: {}", e.getMessage());
            throw e;
        } finally {
            closeEntryQuietly(zipOut, file.relativePath());
        }
    }

    private void closeEntryQuietly(ZipOutputStream zipOut, String path) {
        try {
            zipOut.closeEntry();
        } catch (IOException e) {
            log.trace("Failed to close zip entry for {}", path);
        }
    }
}
