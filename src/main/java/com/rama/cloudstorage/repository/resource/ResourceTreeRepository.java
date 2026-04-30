package com.rama.cloudstorage.repository.resource;

import com.rama.cloudstorage.dto.resource.download.FileDownloadProjection;

import java.util.List;
import java.util.UUID;

public interface ResourceTreeRepository {
    List<UUID> findAllDescantedIds(UUID rootFolderId);

    List<FileDownloadProjection> findDescendantFilesWithRelativePath(UUID rootFolderId);

    void markSubtreeAsDeleting(UUID rootFolderId, UUID userId);
}
