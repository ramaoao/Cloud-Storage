package com.rama.cloudstorage.entity;

import com.github.f4b6a3.uuid.UuidCreator;
import com.rama.cloudstorage.entity.enums.ResourceStatus;
import com.rama.cloudstorage.entity.enums.ResourceType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "resources")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Resource {
    @Id
    @Column(columnDefinition = "uuid", updatable = false)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    Resource parent;

    @Column(nullable = false)
    String name;

    Long size;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    ResourceType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    ResourceStatus status;

    @Column(unique = true, updatable = false)
    UUID objectKey;

    @Version
    Long version;

    @CreationTimestamp
    @Column(updatable = false)
    Instant createdAt;

    @UpdateTimestamp
    Instant updatedAt;

    public static Resource createFile(User user, Resource parent, String name, Long size) {
        return Resource.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .user(user)
                .parent(parent)
                .name(name)
                .size(size)
                .type(ResourceType.FILE)
                .status(ResourceStatus.UPLOADING)
                .objectKey(UuidCreator.getTimeOrderedEpoch())
                .build();
    }

    public static Resource createDirectory(User user, Resource parent, String name) {
        return Resource.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .user(user)
                .parent(parent)
                .name(name)
                .size(null)
                .type(ResourceType.DIRECTORY)
                .status(ResourceStatus.READY)
                .objectKey(null)
                .build();
    }
}
