package com.rama.cloudstorage.component.minio;

import com.rama.cloudstorage.exception.minio.StorageOperationException;
import com.rama.cloudstorage.util.MinioAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MinioOperationHandler {

    public <T> T execute(MinioAction<T> action, String errorMessage) {
        try {
            return action.execute();
        } catch (Exception e) {
            log.error("{}. Details: {}", errorMessage, e.getMessage());
            throw new StorageOperationException(errorMessage, e);
        }
    }
}
