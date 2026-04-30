package com.rama.cloudstorage.dto.resource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"parent", "name", "size", "type"})
public record ResourceInfoDto(
        UUID id,
        UUID parentId,
        String name,
        Long size,
        String type,
        Instant updatedAt
) {
}
