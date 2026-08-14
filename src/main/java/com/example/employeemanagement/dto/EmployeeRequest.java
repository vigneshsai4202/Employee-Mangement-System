package com.example.employeemanagement.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/*
 * EmployeeRequest is the inbound DTO — it represents what the client sends us.
 * Used for both POST (create) and PUT (update) requests.
 *
 * WHY LOMBOK HERE:
 * @Getter/@Setter → Jackson (the JSON library) needs getters to serialize
 *                   and setters to deserialize JSON into this object.
 * @NoArgsConstructor → Jackson requires a no-arg constructor to instantiate
 *                      the object before setting fields via setters.
 * @AllArgsConstructor + @Builder → convenient for creating instances in tests.
 *
 * Notice we do NOT use @Entity here. This is a plain Java class (POJO).
 * It has no connection to Hibernate or the database.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeRequest {

    /*
     * @NotBlank:
     * What    → Validates that the field is not null, not empty (""), and not blank ("   ").
     * Why     → @NotNull only checks for null. @NotEmpty allows "   " (spaces only).
     *            @NotBlank is the strictest and correct choice for name fields.
     * When    → Triggered when @Valid is placed on the controller method parameter.
     * message → Custom error message returned in the validation error response.
     */
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    /*
     * @Email:
     * What    → Validates that the string is a well-formed email address.
     * Why     → Without this, "notanemail" would pass through to the database.
     * Internal → Uses a regex pattern to check for the presence of @ and a domain.
     *
     * @NotBlank is also needed because @Email alone allows null values.
     * Both annotations must be present together for full validation.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    /*
     * @Pattern:
     * What    → Validates the field against a regular expression.
     * Why     → Phone numbers have a specific format. We allow:
     *            +91-9876543210  (international with country code)
     *            9876543210      (10 digits)
     *            +1 (555) 123-4567 (US format)
     * The regex allows digits, spaces, hyphens, parentheses, and a leading +.
     * phoneNumber is optional (no @NotBlank), so @Pattern only fires if a value is provided.
     */
    @Pattern(
        regexp = "^[+]?[0-9\\s\\-().]{7,20}$",
        message = "Phone number must be valid (7-20 characters, digits, spaces, +, -, () allowed)"
    )
    private String phoneNumber;

    @NotBlank(message = "Department is required")
    @Size(max = 100, message = "Department must not exceed 100 characters")
    private String department;

    @NotBlank(message = "Job title is required")
    @Size(max = 100, message = "Job title must not exceed 100 characters")
    private String jobTitle;

    /*
     * @NotNull:
     * What    → Validates that the field is not null.
     * Why     → We use @NotNull (not @NotBlank) for salary because BigDecimal
     *            is not a String — @NotBlank only works on CharSequence types.
     *
     * @Positive:
     * What    → Validates that the number is strictly greater than zero.
     * Why     → A salary of 0 or negative makes no business sense.
     * Note    → @PositiveOrZero would allow 0. @Positive requires > 0.
     *
     * @Digits:
     * What    → Validates the number of integer and fraction digits.
     * Why     → Prevents someone sending salary=999999999999999.99 which
     *            would overflow our DECIMAL(15,2) column.
     */
    @NotNull(message = "Salary is required")
    @Positive(message = "Salary must be a positive value")
    @Digits(integer = 13, fraction = 2, message = "Salary must have at most 13 integer digits and 2 decimal places")
    private BigDecimal salary;

    /*
     * @NotNull for LocalDate:
     * Same reason as salary — @NotBlank doesn't work on non-String types.
     *
     * @PastOrPresent:
     * What    → Validates that the date is today or in the past.
     * Why     → An employee cannot have a hire date in the future.
     *            This is a business rule enforced at the API layer.
     */
    @NotNull(message = "Hire date is required")
    @PastOrPresent(message = "Hire date cannot be in the future")
    private LocalDate hireDate;
}
