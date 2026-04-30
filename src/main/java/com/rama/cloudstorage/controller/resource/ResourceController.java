package com.rama.cloudstorage.controller.resource;

import com.rama.cloudstorage.dto.resource.MoveResourceRequest;
import com.rama.cloudstorage.dto.resource.RenameResourceRequest;
import com.rama.cloudstorage.dto.resource.ResourceInfoDto;
import com.rama.cloudstorage.dto.resource.download.DownloadResponse;
import com.rama.cloudstorage.dto.resource.upload.FileUploadConfirmRequest;
import com.rama.cloudstorage.dto.resource.upload.FileUploadInitRequest;
import com.rama.cloudstorage.dto.resource.upload.PresignedUploadResponse;
import com.rama.cloudstorage.security.UserPrincipal;
import com.rama.cloudstorage.service.resource.ResourceDeletionService;
import com.rama.cloudstorage.service.resource.ResourceDownloadService;
import com.rama.cloudstorage.service.resource.ResourceManagementService;
import com.rama.cloudstorage.service.resource.ResourceUploadService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/resource")
public class ResourceController {

    private final ResourceManagementService managementService;
    private final ResourceUploadService uploadService;
    private final ResourceDownloadService downloadService;
    private final ResourceDeletionService deletionService;

    @GetMapping("/{id}")
    public ResponseEntity<ResourceInfoDto> getResourceInfo(@PathVariable UUID id,
                                                           @AuthenticationPrincipal UserPrincipal currentUser) {

        return ResponseEntity.ok(managementService.getResourceInfo(currentUser.id(), id));
    }

    @GetMapping("/search")
    public ResponseEntity<Slice<ResourceInfoDto>> search(@RequestParam("query") @NotBlank String query,
                                                         @PageableDefault(sort = {"type", "name"}, direction = Sort.Direction.DESC) Pageable pageable,
                                                         @AuthenticationPrincipal UserPrincipal currentUser) {

        return ResponseEntity.ok(managementService.search(currentUser.id(), query, pageable));
    }

    @PostMapping("/upload/init")
    public ResponseEntity<List<PresignedUploadResponse>> initiateUpload(@RequestParam(value = "parentId", required = false) UUID parentId,
                                                                        @RequestBody @Valid List<FileUploadInitRequest> uploadRequests,
                                                                        @AuthenticationPrincipal UserPrincipal currentUser) {

        List<PresignedUploadResponse> responses = uploadService.initiateUpload(currentUser.id(), parentId, uploadRequests);

        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    @PostMapping("/upload/confirm")
    @ResponseStatus(HttpStatus.OK)
    public void confirmUpload(@RequestBody @Valid @Size(max = 1000, message = "Max 1000 files.") List<FileUploadConfirmRequest> confirmRequests,
                              @AuthenticationPrincipal UserPrincipal currentUser) {

        List<UUID> objectKeys = confirmRequests.stream()
                .map(FileUploadConfirmRequest::objectKey)
                .toList();

        uploadService.confirmUploads(currentUser.id(), objectKeys);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<StreamingResponseBody> download(@PathVariable UUID id,
                                                          @AuthenticationPrincipal UserPrincipal currentUser) {

        DownloadResponse response = downloadService.download(currentUser.id(), id);

        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(response.filename(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(response.body());
    }

    @PutMapping("/{id}/move")
    public ResponseEntity<ResourceInfoDto> move(@PathVariable UUID id,
                                                @RequestBody @Valid MoveResourceRequest request,
                                                @AuthenticationPrincipal UserPrincipal currentUser) {

        return ResponseEntity.ok(managementService.move(currentUser.id(), id, request.targetFolderId()));
    }

    @PutMapping("/{id}/rename")
    public ResponseEntity<ResourceInfoDto> rename(@PathVariable UUID id,
                                                  @RequestBody @Valid RenameResourceRequest request,
                                                  @AuthenticationPrincipal UserPrincipal currentUser) {

        return ResponseEntity.ok(managementService.rename(currentUser.id(), id, request.newName()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id,
                       @AuthenticationPrincipal UserPrincipal currentUser) {
        deletionService.deleteResource(currentUser.id(), id);
    }
}