package com.rama.cloudstorage.dto.resource.download;

import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public record DownloadResponse(
        String filename,
        StreamingResponseBody body
) {
}
