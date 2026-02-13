package com.quickbite.product_service.exception;

import com.quickbite.core.api.ApiError;
import com.quickbite.core.dto.ErrorResponse;
import com.quickbite.core.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
        MethodArgumentNotValidException ex
    ) {
        Map<String, String> details = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            details.put(fieldName, errorMessage);
        });

        ApiError apiError = new ApiError(
            "VALIDATION_ERROR",
            "validation failed"
        );

        return build(apiError, HttpStatus.BAD_REQUEST, details);
    }

    @ExceptionHandler(BaseBusinessException.class)
    public ResponseEntity<ErrorResponse> handleBaseBusinessException(
        BaseBusinessException ex
    ) {
        HttpStatus status = resolveStatus(ex);

        if (status.is5xxServerError()) {
            log.error("Business exception", ex);
        }

        return build(ex.getApiError(), status, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
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

    private HttpStatus resolveStatus(BaseBusinessException ex) {
        return switch (ex.getApiError().code()) {
            case "RESOURCE_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "DATA_VALIDATION_ERROR", "VALIDATION_ERROR" -> HttpStatus.BAD_REQUEST;
            case "BUSINESS_RULE_VIOLATION" -> HttpStatus.UNPROCESSABLE_ENTITY;
            case "DATABASE_ERROR" -> HttpStatus.INTERNAL_SERVER_ERROR;
            case "JWT_VALIDATION_ERROR", "TOKEN_ERROR", "INVALID_USER_STATUS" -> HttpStatus.UNAUTHORIZED;
            case "USER_ALREADY_EXISTS" -> HttpStatus.CONFLICT;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}

