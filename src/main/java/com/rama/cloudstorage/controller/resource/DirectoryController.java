package com.rama.cloudstorage.controller.resource;

import com.rama.cloudstorage.dto.resource.ResourceInfoDto;
import com.rama.cloudstorage.dto.resource.directory.CreateFolderRequest;
import com.rama.cloudstorage.security.UserPrincipal;
import com.rama.cloudstorage.service.resource.directory.DirectoryManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/directory")
public class DirectoryController {

    private final DirectoryManagementService directoryService;

    @GetMapping("/root")
    public ResponseEntity<Slice<ResourceInfoDto>> getRootContent(@PageableDefault(sort = {"type", "name"}, direction = Sort.Direction.DESC) Pageable pageable,
                                                                 @AuthenticationPrincipal UserPrincipal currentUser) {

        Slice<ResourceInfoDto> results = directoryService.getDirectoryContent(currentUser.id(), null, pageable);

        return ResponseEntity.ok(results);
    }

    @GetMapping({"/{folderId}"})
    public ResponseEntity<Slice<ResourceInfoDto>> getDirectoryContent(@PathVariable UUID folderId,
                                                                      @PageableDefault(sort = {"type", "name"}, direction = Sort.Direction.DESC) Pageable pageable,
                                                                      @AuthenticationPrincipal UserPrincipal currentUser) {

        Slice<ResourceInfoDto> results = directoryService.getDirectoryContent(currentUser.id(), folderId, pageable);

        return ResponseEntity.ok(results);
    }

    @PostMapping
    public ResponseEntity<ResourceInfoDto> createFolder(@RequestParam(required = false) UUID parentId,
                                                        @RequestBody @Valid CreateFolderRequest request,
                                                        @AuthenticationPrincipal UserPrincipal currentUser) {

        ResourceInfoDto result = directoryService.createFolder(currentUser.id(), parentId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
