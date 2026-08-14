package com.example.employeemanagement.controller;

import com.example.employeemanagement.dto.EmployeeRequest;
import com.example.employeemanagement.dto.EmployeeResponse;
import com.example.employeemanagement.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
 * @Tag: Groups all endpoints in this controller under one section in Swagger UI.
 * name        → the section heading
 * description → shown below the heading
 */
@Tag(name = "Employee Management", description = "APIs for managing employees")
@Slf4j
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    /*
     * Constructor injection — the controller depends on the SERVICE INTERFACE,
     * not the implementation. Spring injects EmployeeServiceImpl automatically
     * because it's the only class implementing EmployeeService.
     * This is the Dependency Inversion Principle in action.
     */
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /*
     * POST /api/employees
     * Status: 201 CREATED
     *
     * WHY 201 and not 200?
     * 200 OK means "request succeeded and here's the result".
     * 201 CREATED specifically means "a new resource was created".
     * Using the correct status code communicates intent clearly to API clients.
     *
     * @RequestBody:
     * What    → Tells Spring to deserialize the HTTP request body JSON
     *            into an EmployeeRequest object using Jackson.
     *
     * @Valid:
     * What    → Triggers Bean Validation on the EmployeeRequest object.
     * Why     → Without @Valid, all the @NotBlank, @Email etc. annotations
     *            on EmployeeRequest are completely ignored.
     * Internal → Spring calls Hibernate Validator before the method body executes.
     *             If any constraint fails, MethodArgumentNotValidException is thrown
     *             and our GlobalExceptionHandler catches it.
     */
    @Operation(summary = "Create a new employee",
               description = "Creates a new employee record. Email must be unique.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Employee created successfully"),
        @ApiResponse(responseCode = "400", description = "Validation failed",
                     content = @Content(schema = @Schema(implementation = com.example.employeemanagement.exception.ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Email already exists",
                     content = @Content(schema = @Schema(implementation = com.example.employeemanagement.exception.ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<EmployeeResponse> createEmployee(
            @Valid @RequestBody EmployeeRequest request) {

        log.info("POST /api/employees - creating employee");
        EmployeeResponse response = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /*
     * GET /api/employees
     * Status: 200 OK
     *
     * Returns all employees. In a real production system you would add
     * pagination here (Pageable), but we keep it simple for now.
     * Returns an empty list [] if no employees exist — never null.
     */
    @Operation(summary = "Get all employees", description = "Returns a list of all employees")
    @ApiResponse(responseCode = "200", description = "List of employees returned successfully")
    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees() {
        log.info("GET /api/employees - fetching all employees");
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    /*
     * GET /api/employees/{id}
     * Status: 200 OK or 404 NOT FOUND
     *
     * @PathVariable:
     * What    → Extracts the {id} segment from the URL and binds it to the parameter.
     * Example → GET /api/employees/5 → id = 5
     */
    @Operation(summary = "Get employee by ID", description = "Returns a single employee by their ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Employee found"),
        @ApiResponse(responseCode = "404", description = "Employee not found",
                     content = @Content(schema = @Schema(implementation = com.example.employeemanagement.exception.ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getEmployeeById(
            @Parameter(description = "ID of the employee to retrieve") @PathVariable Long id) {
        log.info("GET /api/employees/{} - fetching employee", id);
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    /*
     * PUT /api/employees/{id}
     * Status: 200 OK or 404 NOT FOUND or 409 CONFLICT
     *
     * WHY PUT and not PATCH?
     * PUT = full replacement. The client sends the complete employee object.
     * PATCH = partial update. The client sends only the fields to change.
     * We use PUT here for simplicity. PATCH would require handling null fields
     * differently in the mapper (only update fields that are not null).
     */
    @Operation(summary = "Update an employee", description = "Updates all fields of an existing employee")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Employee updated successfully"),
        @ApiResponse(responseCode = "404", description = "Employee not found",
                     content = @Content(schema = @Schema(implementation = com.example.employeemanagement.exception.ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Email already exists",
                     content = @Content(schema = @Schema(implementation = com.example.employeemanagement.exception.ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @Parameter(description = "ID of the employee to update") @PathVariable Long id,
            @Valid @RequestBody EmployeeRequest request) {

        log.info("PUT /api/employees/{} - updating employee", id);
        return ResponseEntity.ok(employeeService.updateEmployee(id, request));
    }

    /*
     * DELETE /api/employees/{id}
     * Status: 204 NO CONTENT or 404 NOT FOUND
     *
     * WHY 204 NO CONTENT?
     * The resource was deleted — there's nothing to return in the body.
     * 204 explicitly tells the client "success, but no response body".
     * Returning 200 with an empty body would also work but is less precise.
     */
    @Operation(summary = "Delete an employee", description = "Permanently deletes an employee by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Employee deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Employee not found",
                     content = @Content(schema = @Schema(implementation = com.example.employeemanagement.exception.ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(
            @Parameter(description = "ID of the employee to delete") @PathVariable Long id) {
        log.info("DELETE /api/employees/{} - deleting employee", id);
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    /*
     * GET /api/employees/search?keyword=john
     * Status: 200 OK
     *
     * @RequestParam:
     * What    → Extracts a query parameter from the URL.
     * Example → /api/employees/search?keyword=john → keyword = "john"
     *
     * defaultValue = "" means if no keyword is provided, return all employees
     * that match an empty string (effectively all employees).
     * required = false means the parameter is optional.
     *
     * WHY /search before /{id}?
     * Spring matches routes top to bottom. If /{id} came first,
     * /search would be treated as id="search" and fail with a type mismatch.
     * Specific routes must always be declared before parameterized ones.
     * Since we declare @GetMapping("/search") as a separate method,
     * Spring handles the disambiguation automatically.
     */
    @Operation(summary = "Search employees",
               description = "Search employees by first name, last name, or email (case-insensitive)")
    @ApiResponse(responseCode = "200", description = "Search results returned")
    @GetMapping("/search")
    public ResponseEntity<List<EmployeeResponse>> searchEmployees(
            @Parameter(description = "Search keyword (matches first name, last name, or email)")
            @RequestParam(required = false, defaultValue = "") String keyword) {

        log.info("GET /api/employees/search?keyword={}", keyword);
        return ResponseEntity.ok(employeeService.searchEmployees(keyword));
    }

    /*
     * GET /api/employees/department/{department}
     * Status: 200 OK
     *
     * Returns all employees in a given department.
     * Returns empty list [] if no employees found in that department.
     */
    @Operation(summary = "Get employees by department",
               description = "Returns all employees belonging to the specified department")
    @ApiResponse(responseCode = "200", description = "Employees returned successfully")
    @GetMapping("/department/{department}")
    public ResponseEntity<List<EmployeeResponse>> getEmployeesByDepartment(
            @Parameter(description = "Department name to filter by") @PathVariable String department) {

        log.info("GET /api/employees/department/{}", department);
        return ResponseEntity.ok(employeeService.getEmployeesByDepartment(department));
    }
}
