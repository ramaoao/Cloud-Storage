package com.rama.cloudstorage.mapper;

import com.rama.cloudstorage.dto.resource.ResourceInfoDto;
import com.rama.cloudstorage.entity.Resource;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ResourceMapper {

    ResourceInfoDto toDto(Resource resource);
}
