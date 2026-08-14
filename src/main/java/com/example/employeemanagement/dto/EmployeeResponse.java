package com.example.employeemanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/*
 * EmployeeResponse is the outbound DTO — it represents what we send back to the client.
 * Used as the return type for all API endpoints.
 *
 * Key design decisions:
 *
 * 1. We include 'id' here because the client needs it to make follow-up requests
 *    (e.g., GET /api/employees/5, PUT /api/employees/5).
 *    We do NOT include it in EmployeeRequest because the client never assigns IDs —
 *    the database generates them.
 *
 * 2. We include 'createdAt' and 'updatedAt' because they are useful audit information
 *    for the client (e.g., "when was this employee record last modified?").
 *    These are read-only — the client can see them but never set them.
 *
 * 3. No validation annotations here. This class is never received from the client,
 *    only sent to the client. Validation only applies to inbound data.
 *
 * 4. No @Entity, no JPA annotations. This is a pure data carrier (POJO).
 *    Jackson serializes this to JSON automatically.
 *
 * WHY @Builder here?
 * In the mapper, we'll construct EmployeeResponse using the builder pattern:
 *   EmployeeResponse.builder()
 *       .id(employee.getId())
 *       .firstName(employee.getFirstName())
 *       ...
 *       .build();
 * This is more readable than a constructor with 11 arguments.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String department;
    private String jobTitle;
    private BigDecimal salary;
    private LocalDate hireDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
