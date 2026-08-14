package com.example.employeemanagement.exception;

/*
 * WHY EXTEND RuntimeException AND NOT Exception?
 *
 * Checked exceptions (extend Exception) force every caller to either
 * catch them or declare them in a throws clause. This creates noise
 * throughout the codebase and doesn't add value for business exceptions.
 *
 * Unchecked exceptions (extend RuntimeException) propagate up the call
 * stack automatically until something catches them — in our case,
 * the GlobalExceptionHandler catches them at the controller boundary.
 *
 * Spring's own exceptions (DataAccessException, etc.) are all unchecked.
 * Following the same convention keeps the codebase consistent.
 */
public class EmployeeNotFoundException extends RuntimeException {

    /*
     * We pass a descriptive message to the parent constructor.
     * This message is what the GlobalExceptionHandler will extract
     * and include in the error response sent to the client.
     *
     * Example: "Employee with ID 10 not found"
     */
    public EmployeeNotFoundException(Long id) {
        super("Employee with ID " + id + " not found");
    }
}
