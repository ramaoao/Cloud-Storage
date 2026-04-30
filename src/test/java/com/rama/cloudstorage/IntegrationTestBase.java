package com.rama.cloudstorage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rama.cloudstorage.entity.User;
import com.rama.cloudstorage.repository.resource.ResourceRepository;
import com.rama.cloudstorage.repository.user.UserRepository;
import com.rama.cloudstorage.service.resource.ResourceManagementService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public abstract class IntegrationTestBase {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRE_SQL_CONTAINER = new PostgreSQLContainer<>("postgres:18.1-alpine");

    @Container
    @ServiceConnection
    static final GenericContainer<?> REDIS_CONTAINER = new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379);

    @Autowired
    protected ResourceRepository resourceRepository;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected ResourceManagementService resourceManagementService;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected User testUser;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(User.create("user", "password"));
    }

    @AfterEach
    void tearDown() {
        resourceRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }
}
