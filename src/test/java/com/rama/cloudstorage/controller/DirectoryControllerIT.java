package com.rama.cloudstorage.controller;

import com.rama.cloudstorage.IntegrationTestBase;
import com.rama.cloudstorage.dto.resource.directory.CreateFolderRequest;
import com.rama.cloudstorage.security.UserPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DirectoryControllerIT extends IntegrationTestBase {

    @Test
    @DisplayName("GET /directory должен вернуть 401 для неавторизированного пользователя")
    void getDirectory_ShouldReturn401_WhenUnauthorized() throws Exception {
        UUID folderId = UUID.randomUUID();

        mockMvc.perform(get("/api/directory/{folderId}", folderId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /directory должен вернуть 201 для авторизованного пользователя")
    void postDirectory_ShouldReturn201_WhenAuthorized() throws Exception {
        UserPrincipal userPrincipal = new UserPrincipal(testUser.getId(), "user", "password");

        CreateFolderRequest request = new CreateFolderRequest("new-folder");

        mockMvc.perform(post("/api/directory")
                .with(user(userPrincipal))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}
