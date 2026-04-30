package com.rama.cloudstorage.repository.resource;

import com.rama.cloudstorage.entity.Resource;
import com.rama.cloudstorage.entity.enums.ResourceStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, UUID>, ResourceTreeRepository {

    @Query("""
           SELECT r FROM Resource r
           JOIN FETCH r.user
           WHERE r.user.id = :userId
           AND ((:parentId IS NULL AND r.parent IS NULL) OR r.parent.id = :parentId)
           AND r.status = 'READY'
           """)
    Slice<Resource> findContentsByParentId(@Param("userId") UUID userId,
                                          @Param("parentId") UUID parentId,
                                          Pageable pageable);

    @Query("""
           SELECT r FROM Resource r
           WHERE r.user.id = :userId
           AND r.parent IS NULL
           AND r.name IN :names
           AND r.type = 'DIRECTORY'
           AND r.status != 'DELETING'
           """)
    List<Resource> findRootDirectoriesByNames(@Param("userId") UUID userId,
                                              @Param("names") Collection<String> names);

    @Query("""
           SELECT r FROM Resource r
           WHERE r.user.id = :userId
           AND r.parent.id IN :parentIds
           AND r.name IN :names
           AND r.type = 'DIRECTORY'
           AND r.status != 'DELETING'
           """)
    List<Resource> findDirectoriesByParentsAndNames(@Param("userId") UUID userId,
                                                    @Param("parentIds") Collection<UUID> parentIds,
                                                    @Param("names") Collection<String> names);

    @Query("""
           SELECT r FROM Resource r
           WHERE r.user.id = :userId
           AND r.status = 'READY'
           AND r.name ILIKE CONCAT('%', :query, '%')
           """)
    Slice<Resource> searchByName(@Param("userId") UUID userId,
                                 @Param("query") String  query,
                                 Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
           UPDATE Resource r
           SET r.status = :targetStatus,
               r.updatedAt = CURRENT_TIMESTAMP,
               r.version = r.version + 1
           WHERE r.user.id = :userId
           AND r.objectKey IN :objectKeys
           AND r.status = 'UPLOADING'
           """)
    int confirmUploads(@Param("userId") UUID userId,
                       @Param("objectKeys") List<UUID> objectKeys,
                       @Param("targetStatus") ResourceStatus targetStatus);

    @Query("""
           SELECT r FROM Resource r
           WHERE r.status = 'UPLOADING'
           AND r.createdAt < :threshold
           """)
    Slice<Resource> findStaleUploads(@Param("threshold") Instant threshold, Pageable pageable);

    @Query("""
           SELECT r FROM Resource r
           WHERE r.status = 'DELETING'
           AND r.updatedAt < :threshold
           """)
    Slice<Resource> findPendingDeleting(@Param("threshold") Instant threshold, Pageable pageable);

    @Modifying
    @Transactional
    @Query("""
           DELETE FROM Resource r WHERE r.id IN :ids
           """)
    void deleteByIdsInBatch(@Param("ids") List<UUID> ids);
}
