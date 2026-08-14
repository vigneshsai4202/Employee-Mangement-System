package com.example.employeemanagement.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

/*
 * @RestControllerAdvice:
 * What    → A global interceptor that catches exceptions thrown by any
 *            @RestController in the application.
 * Why     → Without this, every controller method would need its own try/catch.
 *            This centralizes all exception handling in one place.
 * Internal → Combines @ControllerAdvice (intercepts controllers) and
 *             @ResponseBody (serializes return values to JSON).
 * Where   → One per application, in the exception package.
 *
 * Flow:
 *   Controller method throws exception
 *        ↓
 *   Spring looks for a matching @ExceptionHandler in this class
 *        ↓
 *   Handler method builds ErrorResponse
 *        ↓
 *   ResponseEntity returned to client as JSON
 *   (stack trace never reaches the client)
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /*
     * Handles: EmployeeNotFoundException
     * Status:  404 NOT FOUND
     * When:    GET/PUT/DELETE with an id that doesn't exist in the database
     */
    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEmployeeNotFound(
            EmployeeNotFoundException ex,
            HttpServletRequest request) {

        log.warn("Employee not found: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Employee Not Found")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /*
     * Handles: DuplicateEmailException
     * Status:  409 CONFLICT
     * When:    POST or PUT with an email that already belongs to another employee
     *
     * WHY 409 CONFLICT and not 400 BAD REQUEST?
     * The request itself is valid (email format is correct).
     * The problem is a conflict with existing server state.
     * 409 communicates "your request is fine, but it conflicts with what's already here".
     */
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmail(
            DuplicateEmailException ex,
            HttpServletRequest request) {

        log.warn("Duplicate email: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Duplicate Email")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /*
     * Handles: MethodArgumentNotValidException
     * Status:  400 BAD REQUEST
     * When:    @Valid fails on an @RequestBody — one or more fields violate constraints
     *
     * This exception contains a list of FieldError objects, one per violated constraint.
     * We extract them into a Map<fieldName, errorMessage> for a clean response.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        /*
         * getBindingResult().getFieldErrors() returns all field-level violations.
         * We collect them into a Map: { "firstName": "First name is required", ... }
         * If multiple violations exist for the same field, the last one wins
         * (Collectors.toMap would throw — we use toMap with merge function).
         */
        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (existing, replacement) -> existing
                ));

        log.warn("Validation failed: {}", fieldErrors);

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Request contains invalid fields")
                .path(request.getRequestURI())
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /*
     * Handles: DataIntegrityViolationException
     * Status:  409 CONFLICT
     * When:    A DB constraint is violated (e.g., unique constraint on email).
     *          This is a safety net — our service layer checks for duplicates first,
     *          but if two concurrent requests slip through simultaneously,
     *          the DB unique constraint fires and Spring wraps it in this exception.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {

        log.error("Data integrity violation: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Data Integrity Violation")
                .message("A database constraint was violated. Please check your input.")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /*
     * Handles: NoResourceFoundException
     * Status:  404 NOT FOUND
     * When:    Browser hits "/" or "/favicon.ico" — no static resources exist.
     *          Without this, it falls through to the 500 handler.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(
            NoResourceFoundException ex,
            HttpServletRequest request) {

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /*
     * Handles: Any other unhandled exception
     * Status:  500 INTERNAL SERVER ERROR
     * When:    Something unexpected happens that we didn't anticipate.
     *
     * IMPORTANT: We log the full stack trace here (for our logs) but
     * we NEVER send the stack trace to the client. The client only sees
     * a generic message. This prevents leaking internal implementation details.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        log.error("Unexpected error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred. Please try again later.")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
