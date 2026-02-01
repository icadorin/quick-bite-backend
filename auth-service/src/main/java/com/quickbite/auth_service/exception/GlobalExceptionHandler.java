package com.quickbite.auth_service.exception;

import com.quickbite.core.api.ApiError;
import com.quickbite.core.dto.ErrorResponse;
import com.quickbite.core.exception.BaseBusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
        MethodArgumentNotValidException ex
    ) {
        Map<String, String> details = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            if (error instanceof FieldError fieldError) {
                details.put(
                    fieldError.getField(),
                    fieldError.getDefaultMessage()
                );
            } else {
                details.put(
                    error.getObjectName(),
                    error.getDefaultMessage()
                );
            }
        });

        ApiError apiError = new ApiError(
            "VALIDATION_ERROR",
            "Validation failed"
        );

        return build(apiError, HttpStatus.BAD_REQUEST, details);
    }

    @ExceptionHandler(BaseBusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
        BaseBusinessException ex
    ) {
        HttpStatus status = resolveStatus(ex.getApiError().code());

        if (status.is5xxServerError()) {
            log.error("Business exception", ex);
        }

        return build(ex.getApiError(), status, null);
    }

    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        ApiError apiError = new ApiError(
            "UNAUTHORIZED",
            "Authentication failed: " + ex.getMessage()
        );

        return build(apiError, HttpStatus.UNAUTHORIZED, null);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        ApiError apiError = new ApiError(
            "ACCESS_DENIED",
            "You do not have permission to access this resource"
        );

        return build(apiError, HttpStatus.FORBIDDEN, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneticException(Exception ex) {
        log.error("Unexpected error", ex);

        ApiError apiError = new ApiError(
            "GENERIC_ERROR",
            "Unexpected internal error"
        );

        return build(apiError, HttpStatus.INTERNAL_SERVER_ERROR, null);
    }

    private ResponseEntity<ErrorResponse> build(
        ApiError apiError,
        HttpStatus status,
        Map<String, String> details
    ) {
        return ResponseEntity.status(status).body(
            ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(apiError.message())
                .errorCode(apiError.code())
                .details(details)
                .build()
        );
    }

    private HttpStatus resolveStatus(String errorCode) {
        return switch (errorCode) {
            case "USER_ALREADY_EXISTS" -> HttpStatus.CONFLICT;
            case "INVALID_USER_STATUS" -> HttpStatus.FORBIDDEN;
            case "JWT_VALIDATION_ERROR", "TOKEN_ERROR", "UNAUTHORIZED" -> HttpStatus.UNAUTHORIZED;
            case "ACCESS_DENIED" -> HttpStatus.FORBIDDEN;
            case "DATA_VALIDATION_ERROR", "VALIDATION_ERROR" -> HttpStatus.BAD_REQUEST;
            case "BUSINESS_RULE_VIOLATION" -> HttpStatus.UNPROCESSABLE_ENTITY;
            case "RESOURCE_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "DATABASE_ERROR" -> HttpStatus.INTERNAL_SERVER_ERROR;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
