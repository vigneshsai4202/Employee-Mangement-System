package com.example.employeemanagement.service;

import com.example.employeemanagement.dto.EmployeeRequest;
import com.example.employeemanagement.dto.EmployeeResponse;
import com.example.employeemanagement.entity.Employee;
import com.example.employeemanagement.exception.DuplicateEmailException;
import com.example.employeemanagement.exception.EmployeeNotFoundException;
import com.example.employeemanagement.mapper.EmployeeMapper;
import com.example.employeemanagement.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/*
 * @ExtendWith(MockitoExtension.class):
 * What    → Integrates Mockito with JUnit 5.
 * Why     → Enables @Mock and @InjectMocks annotations to work automatically.
 *            Without this, you'd have to call MockitoAnnotations.openMocks(this) manually.
 * Internal → Before each test, Mockito creates mock objects for @Mock fields
 *             and injects them into the @InjectMocks target.
 */
@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    /*
     * @Mock:
     * What    → Creates a mock (fake) implementation of the interface/class.
     * Why     → We don't want real database calls in unit tests.
     *            Mocks let us control exactly what the repository returns.
     * Internal → Mockito creates a proxy that records method calls and
     *             returns default values (null, 0, false, empty collections)
     *             unless you configure it with when().thenReturn().
     */
    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    /*
     * @InjectMocks:
     * What    → Creates a real instance of EmployeeServiceImpl and injects
     *            the @Mock fields into it via constructor injection.
     * Why     → We want to test the REAL service logic, just with fake dependencies.
     */
    @InjectMocks
    private EmployeeServiceImpl employeeService;

    // Test data — reused across multiple tests
    private EmployeeRequest request;
    private Employee employee;
    private EmployeeResponse response;

    /*
     * @BeforeEach:
     * What    → Runs before every single @Test method.
     * Why     → Avoids duplicating test data setup in every test.
     *            Each test starts with a fresh, consistent state.
     */
    @BeforeEach
    void setUp() {
        request = EmployeeRequest.builder()
                .firstName("John")
                .lastName("Smith")
                .email("john.smith@example.com")
                .phoneNumber("9876543210")
                .department("Engineering")
                .jobTitle("Software Engineer")
                .salary(new BigDecimal("75000.00"))
                .hireDate(LocalDate.of(2024, 1, 15))
                .build();

        employee = Employee.builder()
                .id(1L)
                .firstName("John")
                .lastName("Smith")
                .email("john.smith@example.com")
                .phoneNumber("9876543210")
                .department("Engineering")
                .jobTitle("Software Engineer")
                .salary(new BigDecimal("75000.00"))
                .hireDate(LocalDate.of(2024, 1, 15))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        response = EmployeeResponse.builder()
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
    // createEmployee tests
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("Should create employee successfully when email is unique")
    void createEmployee_Success() {
        /*
         * ARRANGE — configure mocks to return specific values
         *
         * when(...).thenReturn(...) tells Mockito:
         * "when this method is called with these args, return this value"
         */
        when(employeeRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(employeeMapper.toEntity(request)).thenReturn(employee);
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(employeeMapper.toResponse(employee)).thenReturn(response);

        // ACT — call the real method under test
        EmployeeResponse result = employeeService.createEmployee(request);

        // ASSERT — verify the result and interactions
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("john.smith@example.com");

        /*
         * verify() checks that a mock method was called exactly once.
         * This confirms the service actually called save() — not just returned
         * a hardcoded value.
         */
        verify(employeeRepository, times(1)).save(employee);
        verify(employeeMapper, times(1)).toResponse(employee);
    }

    @Test
    @DisplayName("Should throw DuplicateEmailException when email already exists")
    void createEmployee_DuplicateEmail_ThrowsException() {
        when(employeeRepository.existsByEmail(request.getEmail())).thenReturn(true);

        /*
         * assertThatThrownBy verifies that the code inside the lambda
         * throws the expected exception type with the expected message.
         * This is AssertJ's fluent way of testing exceptions.
         */
        assertThatThrownBy(() -> employeeService.createEmployee(request))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("john.smith@example.com");

        // Verify save() was NEVER called — we should have stopped before reaching it
        verify(employeeRepository, never()).save(any());
    }

    // ─────────────────────────────────────────────
    // getAllEmployees tests
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("Should return list of all employees")
    void getAllEmployees_ReturnsList() {
        when(employeeRepository.findAll()).thenReturn(List.of(employee));
        when(employeeMapper.toResponse(employee)).thenReturn(response);

        List<EmployeeResponse> result = employeeService.getAllEmployees();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFirstName()).isEqualTo("John");
        verify(employeeRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no employees exist")
    void getAllEmployees_ReturnsEmptyList() {
        when(employeeRepository.findAll()).thenReturn(List.of());

        List<EmployeeResponse> result = employeeService.getAllEmployees();

        assertThat(result).isEmpty();
    }

    // ─────────────────────────────────────────────
    // getEmployeeById tests
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("Should return employee when valid ID is provided")
    void getEmployeeById_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeMapper.toResponse(employee)).thenReturn(response);

        EmployeeResponse result = employeeService.getEmployeeById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should throw EmployeeNotFoundException when ID does not exist")
    void getEmployeeById_NotFound_ThrowsException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getEmployeeById(99L))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessageContaining("99");

        verify(employeeMapper, never()).toResponse(any());
    }

    // ─────────────────────────────────────────────
    // updateEmployee tests
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("Should update employee successfully")
    void updateEmployee_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.existsByEmailAndIdNot(request.getEmail(), 1L)).thenReturn(false);
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(employeeMapper.toResponse(employee)).thenReturn(response);

        EmployeeResponse result = employeeService.updateEmployee(1L, request);

        assertThat(result).isNotNull();
        /*
         * verify that updateEntityFromRequest was called on the mapper —
         * this confirms the service mutated the existing entity, not created a new one.
         */
        verify(employeeMapper, times(1)).updateEntityFromRequest(request, employee);
        verify(employeeRepository, times(1)).save(employee);
    }

    @Test
    @DisplayName("Should throw EmployeeNotFoundException when updating non-existent employee")
    void updateEmployee_NotFound_ThrowsException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.updateEmployee(99L, request))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessageContaining("99");

        verify(employeeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw DuplicateEmailException when updating with an email belonging to another employee")
    void updateEmployee_DuplicateEmail_ThrowsException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.existsByEmailAndIdNot(request.getEmail(), 1L)).thenReturn(true);

        assertThatThrownBy(() -> employeeService.updateEmployee(1L, request))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("john.smith@example.com");

        verify(employeeRepository, never()).save(any());
    }

    // ─────────────────────────────────────────────
    // deleteEmployee tests
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("Should delete employee successfully when ID exists")
    void deleteEmployee_Success() {
        when(employeeRepository.existsById(1L)).thenReturn(true);

        employeeService.deleteEmployee(1L);

        verify(employeeRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw EmployeeNotFoundException when deleting non-existent employee")
    void deleteEmployee_NotFound_ThrowsException() {
        when(employeeRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> employeeService.deleteEmployee(99L))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessageContaining("99");

        verify(employeeRepository, never()).deleteById(anyLong());
    }

    // ─────────────────────────────────────────────
    // searchEmployees tests
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("Should return matching employees for search keyword")
    void searchEmployees_ReturnsResults() {
        when(employeeRepository.searchEmployees("john")).thenReturn(List.of(employee));
        when(employeeMapper.toResponse(employee)).thenReturn(response);

        List<EmployeeResponse> result = employeeService.searchEmployees("john");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFirstName()).isEqualTo("John");
    }

    @Test
    @DisplayName("Should return employees filtered by department")
    void getEmployeesByDepartment_ReturnsResults() {
        when(employeeRepository.findByDepartment("Engineering")).thenReturn(List.of(employee));
        when(employeeMapper.toResponse(employee)).thenReturn(response);

        List<EmployeeResponse> result = employeeService.getEmployeesByDepartment("Engineering");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDepartment()).isEqualTo("Engineering");
        verify(employeeRepository, times(1)).findByDepartment("Engineering");
    }
}
