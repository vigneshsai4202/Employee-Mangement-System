package com.example.employeemanagement.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

/*
 * @JsonInclude(JsonInclude.Include.NON_NULL):
 * What    → Tells Jackson to skip fields that are null when serializing to JSON.
 * Why     → The 'fieldErrors' map is only relevant for validation errors (400).
 *            For a 404 or 409, fieldErrors is null — we don't want to send
 *            "fieldErrors": null in the response. This annotation omits it cleanly.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

    /*
     * fieldErrors is only populated for validation failures (400 Bad Request).
     * Key   → field name (e.g., "firstName")
     * Value → validation message (e.g., "First name is required")
     */
    private Map<String, String> fieldErrors;
}
