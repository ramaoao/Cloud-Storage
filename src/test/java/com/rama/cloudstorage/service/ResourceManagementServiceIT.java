package com.rama.cloudstorage.service;

import com.github.f4b6a3.uuid.UuidCreator;
import com.rama.cloudstorage.IntegrationTestBase;
import com.rama.cloudstorage.dto.resource.ResourceInfoDto;
import com.rama.cloudstorage.entity.Resource;
import com.rama.cloudstorage.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ResourceManagementServiceIT extends IntegrationTestBase {

    @Test
    @DisplayName("getResourceInfo должно возращать DTO, если ресурс существует и принадлежит пользователю")
    void getResourceInfo_ShouldReturnDto_WhenResourceExistsAndBelongsToUser() {
        Resource resource = Resource.createDirectory(testUser, null, "test-folder");

        resourceRepository.save(resource);

        ResourceInfoDto result = resourceManagementService.getResourceInfo(testUser.getId(), resource.getId());

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("test-folder");
        assertThat(result.id()).isEqualTo(resource.getId());
    }

    @Test
    @DisplayName("getResourceInfo должно возращать DTO, если ресурс существует и принадлежит пользователю")
    void getResourceInfo_ShouldThrowException_WhenResourceBelongsToAnotherUser() {
        Resource resource = Resource.createDirectory(testUser, null, "test-folder");

        resourceRepository.save(resource);

        UUID otherUserId = UuidCreator.getTimeOrderedEpoch();

        assertThrows(RuntimeException.class, () ->
                resourceManagementService.getResourceInfo(otherUserId, resource.getId()));
    }

    @Test
    @DisplayName("search должен по запрос возвращать соотвествующие ресурсы")
    void search_ShouldReturnMatchedResources() {
        resourceRepository.save(Resource.createDirectory(testUser, null, "work-document"));
        resourceRepository.save(Resource.createDirectory(testUser, null, "homework"));
        resourceRepository.save(Resource.createDirectory(testUser, null, "da"));

        Slice<Resource> result = resourceRepository.searchByName(testUser.getId(), "work", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(Resource::getName)
                .containsExactlyInAnyOrder("work-document", "homework");
    }

    @Test
    @DisplayName("search не должен возвращать ресурсы других пользователей")
    void search_ShouldNotReturnOtherUsersResources() {
        resourceRepository.save(Resource.createDirectory(testUser, null, "work-document"));

        User otherUser = userRepository.save(User.create("otherUser", "password"));
        resourceRepository.save(Resource.createDirectory(otherUser, null, "work"));

        Slice<Resource> result = resourceRepository.searchByName(testUser.getId(), "work", PageRequest.of(0,10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getName()).isEqualTo("work-document");
    }
}
