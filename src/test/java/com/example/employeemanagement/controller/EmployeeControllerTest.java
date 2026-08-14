package com.example.employeemanagement.controller;

import com.example.employeemanagement.dto.EmployeeRequest;
import com.example.employeemanagement.dto.EmployeeResponse;
import com.example.employeemanagement.exception.EmployeeNotFoundException;
import com.example.employeemanagement.exception.GlobalExceptionHandler;
import com.example.employeemanagement.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/*
 * @WebMvcTest(EmployeeController.class):
 * What    → Loads ONLY the web layer (Controller, ExceptionHandler, filters).
 *            Does NOT load Service, Repository, or database configuration.
 * Why     → Faster than @SpringBootTest (full context). We only want to test
 *            HTTP behavior — routing, status codes, request/response JSON.
 * Internal → Creates a MockMvc instance and a minimal Spring context
 *             containing only web-related beans.
 *
 * @Import(GlobalExceptionHandler.class):
 * Why     → @WebMvcTest doesn't automatically pick up @RestControllerAdvice
 *            classes outside the tested controller's package in all versions.
 *            Explicitly importing it ensures our exception handler is active
 *            during tests so we can verify error responses.
 */
@WebMvcTest(EmployeeController.class)
@Import(GlobalExceptionHandler.class)
class EmployeeControllerTest {

    /*
     * MockMvc:
     * What    → Simulates HTTP requests without starting a real server.
     * Why     → Fast, no network overhead, full control over requests/responses.
     * How     → perform() sends a fake HTTP request through the full Spring MVC
     *            pipeline (filters → dispatcher servlet → controller → response).
     */
    @Autowired
    private MockMvc mockMvc;

    /*
     * @MockBean:
     * What    → Creates a Mockito mock AND registers it as a Spring bean
     *            in the test application context.
     * Why     → @WebMvcTest doesn't load the service layer. The controller
     *            needs an EmployeeService bean to be injected — @MockBean
     *            provides a fake one that we can configure with when().
     * Difference from @Mock:
     *   @Mock      → plain Mockito mock, not registered in Spring context
     *   @MockBean  → Mockito mock registered as a Spring bean (needed for @WebMvcTest)
     */
    @MockBean
    private EmployeeService employeeService;

    private ObjectMapper objectMapper;
    private EmployeeRequest validRequest;
    private EmployeeResponse employeeResponse;

    @BeforeEach
    void setUp() {
        /*
         * ObjectMapper is Jackson's JSON serializer/deserializer.
         * We register JavaTimeModule so it can handle LocalDate and LocalDateTime.
         * Without this, Jackson throws an error when serializing Java 8 date types.
         */
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        validRequest = EmployeeRequest.builder()
                .firstName("John")
                .lastName("Smith")
                .email("john.smith@example.com")
                .phoneNumber("9876543210")
                .department("Engineering")
                .jobTitle("Software Engineer")
                .salary(new BigDecimal("75000.00"))
                .hireDate(LocalDate.of(2024, 1, 15))
                .build();

        employeeResponse = EmployeeResponse.builder()
                .id(1L)
                .firstName("John")
                .lastName("Smith")
                .email("john.smith@example.com")
                .department("Engineering")
                .jobTitle("Software Engineer")
                .salary(new BigDecimal("75000.00"))
                .hireDate(LocalDate.of(2024, 1, 15))
                .build();
    }

    // ─────────────────────────────────────────────
    // POST /api/employees
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/employees - should return 201 with created employee")
    void createEmployee_Returns201() throws Exception {
        when(employeeService.createEmployee(any(EmployeeRequest.class)))
                .thenReturn(employeeResponse);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                /*
                 * .andExpect() chains assertions on the response.
                 * status().isCreated()     → HTTP 201
                 * jsonPath("$.id")         → checks JSON field "id" in response body
                 * jsonPath("$.firstName")  → checks JSON field "firstName"
                 */
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.email").value("john.smith@example.com"));
    }

    @Test
    @DisplayName("POST /api/employees - should return 400 when request is invalid")
    void createEmployee_InvalidRequest_Returns400() throws Exception {
        EmployeeRequest invalidRequest = EmployeeRequest.builder()
                .firstName("")           // blank — violates @NotBlank
                .email("not-an-email")   // violates @Email
                .salary(new BigDecimal("-100")) // violates @Positive
                .build();

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.fieldErrors").exists())
                .andExpect(jsonPath("$.fieldErrors.firstName").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }

    // ─────────────────────────────────────────────
    // GET /api/employees
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/employees - should return 200 with list of employees")
    void getAllEmployees_Returns200() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(List.of(employeeResponse));

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                /*
                 * jsonPath("$") refers to the root of the JSON response.
                 * jsonPath("$[0].firstName") refers to firstName of the first element.
                 */
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("John"));
    }

    // ─────────────────────────────────────────────
    // GET /api/employees/{id}
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/employees/{id} - should return 200 with employee")
    void getEmployeeById_Returns200() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(employeeResponse);

        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.department").value("Engineering"));
    }

    @Test
    @DisplayName("GET /api/employees/{id} - should return 404 when employee not found")
    void getEmployeeById_NotFound_Returns404() throws Exception {
        when(employeeService.getEmployeeById(99L))
                .thenThrow(new EmployeeNotFoundException(99L));

        mockMvc.perform(get("/api/employees/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Employee Not Found"))
                .andExpect(jsonPath("$.message").value("Employee with ID 99 not found"));
    }

    // ─────────────────────────────────────────────
    // PUT /api/employees/{id}
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("PUT /api/employees/{id} - should return 200 with updated employee")
    void updateEmployee_Returns200() throws Exception {
        when(employeeService.updateEmployee(eq(1L), any(EmployeeRequest.class)))
                .thenReturn(employeeResponse);

        mockMvc.perform(put("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("PUT /api/employees/{id} - should return 404 when employee not found")
    void updateEmployee_NotFound_Returns404() throws Exception {
        when(employeeService.updateEmployee(eq(99L), any(EmployeeRequest.class)))
                .thenThrow(new EmployeeNotFoundException(99L));

        mockMvc.perform(put("/api/employees/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ─────────────────────────────────────────────
    // DELETE /api/employees/{id}
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /api/employees/{id} - should return 204 on success")
    void deleteEmployee_Returns204() throws Exception {
        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/employees/{id} - should return 404 when employee not found")
    void deleteEmployee_NotFound_Returns404() throws Exception {
        /*
         * doThrow() is used for void methods.
         * when().thenThrow() only works for methods that return a value.
         * deleteEmployee() returns void, so we use doThrow().when() instead.
         */
        doThrow(new EmployeeNotFoundException(99L))
                .when(employeeService).deleteEmployee(99L);

        mockMvc.perform(delete("/api/employees/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ─────────────────────────────────────────────
    // GET /api/employees/search
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/employees/search - should return matching employees")
    void searchEmployees_Returns200() throws Exception {
        when(employeeService.searchEmployees("john")).thenReturn(List.of(employeeResponse));

        mockMvc.perform(get("/api/employees/search").param("keyword", "john"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("John"));
    }
}
