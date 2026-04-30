package com.rama.cloudstorage.exception.handler;

import com.rama.cloudstorage.exception.InvalidOperationException;
import com.rama.cloudstorage.exception.minio.StorageOperationException;
import com.rama.cloudstorage.exception.resource.ResourceNotFoundException;
import com.rama.cloudstorage.exception.resource.ResourcesAlreadyExistsException;
import com.rama.cloudstorage.exception.resource.directory.DirectoryAlreadyExistsException;
import com.rama.cloudstorage.exception.user.UserAlreadyExistsException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<Map<String, String>> handleUnauthorized(Exception e) {
        log.warn("Authentication failure: {}", e.getMessage());
        return errorResponse(UNAUTHORIZED, "Invalid username or password");
    }

    @ExceptionHandler({UserAlreadyExistsException.class, ResourcesAlreadyExistsException.class, DirectoryAlreadyExistsException.class})
    public ResponseEntity<Map<String, String>> handleAlreadyExistsException(RuntimeException e) {
        log.warn("Conflict detected: {}", e.getMessage());
        return errorResponse(CONFLICT, e.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleResourceNotFoundException(ResourceNotFoundException e) {
        log.warn("Resource not found: {}", e.getMessage());
        return errorResponse(NOT_FOUND, "Resource not found.");
    }

    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<Map<String, String>> handleInvalidOperationException(InvalidOperationException e) {
        log.warn("Invalid operation requested: {}", e.getMessage());
        return errorResponse(BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().iterator().next().getMessage();
        log.warn("Constraint violation: {}", message);
        return errorResponse(BAD_REQUEST, message);
    }

    @ExceptionHandler({StorageOperationException.class, Exception.class})
    public ResponseEntity<Map<String, String>> handleInternalError(Exception e) {
        log.error("Unhandled exception occurred!", e);
        return errorResponse(INTERNAL_SERVER_ERROR, "Something went wrong on our side. We are already looking into it.");
    }

    private ResponseEntity<Map<String, String>> errorResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("message", message));
    }
}
