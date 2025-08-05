package com.company.calendar.exceptions;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.stream.Collectors;

@RestControllerAdvice
@Order(3)
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<BaseErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        BaseErrorResponse response = BaseErrorResponse.builder()
                .success(false)
                .message("Invalid request: " + ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<BaseErrorResponse> handleIllegalState(IllegalStateException ex) {
        BaseErrorResponse response = BaseErrorResponse.builder()
                .success(false)
                .message("Slot unavailable: " + ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<BaseErrorResponse> handleDeserializationError(HttpMessageNotReadableException ex) {
        //This exception is thrown during deserialization (before validation).
        Throwable cause = ex.getCause();
        String message = "Invalid request format.";

        if (cause instanceof InvalidFormatException ife) {
            // Optional: Check if it's about LocalTime
            if (ife.getTargetType().equals(LocalTime.class)) {
                message = "Invalid time format. Please use a valid HH:mm format  between 00:00 and 23:00";
            } else if (ife.getTargetType().equals(LocalDateTime.class)){
                message = "Invalid date time format. Please use a valid yyyy-MM-dd HH:mm:ss format";
            } else {
                message = "Invalid value: " + ife.getValue();
            }
        }

        BaseErrorResponse response = BaseErrorResponse.builder()
                .success(false)
                .message(message)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<BaseErrorResponse> handleHttpMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        BaseErrorResponse response = BaseErrorResponse.builder()
                .success(false)
                .message("Method not allowed: " + ex.getMethod())
                .build();

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        String fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));

        String globalErrors = ex.getBindingResult().getGlobalErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("; "));

        String errorMessages = fieldErrors + (fieldErrors.isEmpty() || globalErrors.isEmpty() ? "" : "; ") + globalErrors;

        BaseErrorResponse response = BaseErrorResponse.builder()
                .success(false)
                .message("Validation failed: " + errorMessages)
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<BaseErrorResponse> handleMissingRequestHeader(MissingRequestHeaderException ex) {
        BaseErrorResponse response = BaseErrorResponse.builder()
                .success(false)
                .message("Missing required header: " + ex.getHeaderName())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(InvalidStartDateTimeException.class)
    public ResponseEntity<BaseErrorResponse> handleInvalidStartDateTimeException(InvalidStartDateTimeException ex) {
        BaseErrorResponse response = BaseErrorResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<BaseErrorResponse> handleNoResourceFound(NoResourceFoundException ex) {
        BaseErrorResponse response = BaseErrorResponse.builder()
                .success(false)
                .message("Resource not found: " + ex.getResourcePath())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<BaseErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        String errorMessages = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));

        BaseErrorResponse response = BaseErrorResponse.builder()
                .success(false)
                .message("Validation failed: " + errorMessages)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<BaseErrorResponse> handleMissingServletRequestParameter(MissingServletRequestParameterException ex) {
        String parameterName = ex.getParameterName();
        String message = "Missing required request parameter: " + parameterName;

        BaseErrorResponse response = BaseErrorResponse.builder()
                .success(false)
                .message("Validation failed: " + message)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<BaseErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String paramName = ex.getName(); // e.g., "date"
        String value = ex.getValue() != null ? ex.getValue().toString() : "null";
        String message;

        if (ex.getRequiredType() == LocalDate.class) {
            message = String.format(
                    "Invalid value '%s' for request parameter '%s'. Expected format: yyyy-MM-dd",
                    value, paramName
            );
        } else {
            String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "Unknown";
            message = String.format(
                    "Invalid value '%s' for request parameter '%s'. Expected type: %s",
                    value, paramName, requiredType
            );
        }

        BaseErrorResponse response = BaseErrorResponse.builder()
                .success(false)
                .message("Validation failed: " + message)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseErrorResponse> handleGeneric(Exception ex) {
        BaseErrorResponse response = BaseErrorResponse.builder()
                .success(false)
                .message("Unexpected error: " + ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
