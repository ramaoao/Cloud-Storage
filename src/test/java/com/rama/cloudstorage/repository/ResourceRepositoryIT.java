package com.rama.cloudstorage.repository;

import com.rama.cloudstorage.IntegrationTestBase;
import com.rama.cloudstorage.entity.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourceRepositoryIT extends IntegrationTestBase {

    @Test
    @DisplayName("Должно найти содержимое корневого католога если родительский идентификатор равен null")
    void shouldFindRootContentWhenParentIdIsNull() {
        Resource rootFolder = Resource.createDirectory(testUser, null, "rootFolder");
        Resource otherRootFolder = Resource.createDirectory(testUser, null, "otherRootFolder");
        resourceRepository.save(rootFolder);
        resourceRepository.save(otherRootFolder);

        Resource childFolder1 = Resource.createDirectory(testUser, rootFolder, "folder1");
        Resource childFolder2 = Resource.createDirectory(testUser, rootFolder, "folder2");

        resourceRepository.save(childFolder1);
        resourceRepository.save(childFolder2);

        Slice<Resource> result = resourceRepository.findContentsByParentId(
                testUser.getId(),
                null,
                PageRequest.of(0, 10)
        );

        assertThat(result).isNotNull();
        assertThat(result.getContent())
                .hasSize(2)
                .extracting(Resource::getName)
                .containsExactlyInAnyOrder("rootFolder", "otherRootFolder");
    }

    @Test
    @DisplayName("Должно найти контент по идентефикатору пользователя и родительском идентефикатору")
    void shouldFindContentsByUserIdAndParentId() {
        Resource rootFolder = Resource.createDirectory(testUser, null, "rootFolder");
        resourceRepository.save(rootFolder);

        Resource childFolder1 = Resource.createDirectory(testUser, rootFolder, "folder1");
        Resource childFolder2 = Resource.createDirectory(testUser, rootFolder, "folder2");

        resourceRepository.save(childFolder1);
        resourceRepository.save(childFolder2);

        Resource otherRootFolder = Resource.createDirectory(testUser, null, "otherRootFolder");
        resourceRepository.save(otherRootFolder);

        Slice<Resource> result = resourceRepository.findContentsByParentId(
                testUser.getId(),
                rootFolder.getId(),
                PageRequest.of(0, 10)
        );

        assertThat(result).isNotNull();
        assertThat(result.getContent())
                .hasSize(2)
                .extracting(Resource::getName)
                .containsExactly("folder1", "folder2");
    }
}
