package com.rama.cloudstorage.repository.resource.impl;

import com.rama.cloudstorage.dto.resource.download.FileDownloadProjection;
import com.rama.cloudstorage.entity.enums.ResourceStatus;
import com.rama.cloudstorage.repository.resource.ResourceTreeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ResourceTreeRepositoryImpl implements ResourceTreeRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<UUID> findAllDescantedIds(UUID rootFolderId) {
        String sql = """
                WITH RECURSIVE tree AS (
                    SELECT id FROM resources WHERE id = ?
                
                    UNION ALL
                
                    SELECT r.id FROM resources r
                    INNER JOIN tree t ON r.parent_id = t.id
                )
                SELECT id FROM tree
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getObject("id", UUID.class), rootFolderId);
    }

    @Override
    public List<FileDownloadProjection> findDescendantFilesWithRelativePath(UUID rootFolderId) {
        String sql = """
                WITH RECURSIVE tree AS (
                    SELECT id, CAST(name AS TEXT) AS relative_path, object_key, type
                    FROM resources
                    WHERE parent_id = CAST(? AS uuid) AND status = 'READY'
                
                    UNION ALL
                
                    SELECT r.id, CAST(t.relative_path || '/' || r.name AS VARCHAR), r.object_key, r.type
                    FROM resources r
                    INNER JOIN tree t ON r.parent_id = t.id
                    WHERE r.status = 'READY'
                )
                SELECT relative_path, object_key
                FROM tree
                WHERE type = 'FILE' AND object_key IS NOT NULL;
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new FileDownloadProjection(
                rs.getString("relative_path"),
                rs.getObject("object_key", UUID.class)
        ), rootFolderId);
    }

    @Override
    public void markSubtreeAsDeleting(UUID rootFolderId, UUID userId) {
        String sql = """
                WITH RECURSIVE tree AS (
                    SELECT id FROM resources
                    WHERE id = ?
                    AND user_id = ?
                
                    UNION ALL
                
                    SELECT r.id FROM resources r
                    INNER JOIN tree t ON r.parent_id = t.id
                )
                UPDATE resources
                SET status = ?,
                    updated_at = CURRENT_TIMESTAMP, version = version + 1
                    WHERE id IN (SELECT id FROM tree ORDER BY id) AND user_id = ?
                """;

        jdbcTemplate.update(sql, rootFolderId, userId, ResourceStatus.DELETING.name(), userId);
    }
}
