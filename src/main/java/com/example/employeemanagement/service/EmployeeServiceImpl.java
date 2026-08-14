package com.example.employeemanagement.service;

import com.example.employeemanagement.dto.EmployeeRequest;
import com.example.employeemanagement.dto.EmployeeResponse;
import com.example.employeemanagement.entity.Employee;
import com.example.employeemanagement.exception.DuplicateEmailException;
import com.example.employeemanagement.exception.EmployeeNotFoundException;
import com.example.employeemanagement.mapper.EmployeeMapper;
import com.example.employeemanagement.repository.EmployeeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/*
 * @Slf4j (Lombok):
 * Generates a private static final Logger field:
 *   private static final Logger log = LoggerFactory.getLogger(EmployeeServiceImpl.class);
 * Saves you from writing this boilerplate in every class.
 * We use log.info(), log.warn(), log.error(), log.debug() throughout.
 *
 * @Service:
 * What    → Specialization of @Component. Marks this as a service bean.
 * Why     → Semantically communicates this class holds business logic.
 *            Spring registers it as a singleton bean in the ApplicationContext.
 * Internal → Same as @Component functionally, but more descriptive.
 */
@Slf4j
@Service
/*
 * @Transactional at class level:
 * What    → Wraps every public method in this class in a database transaction.
 * Why     → Ensures that if any operation fails midway, the entire operation
 *            is rolled back. Prevents partial writes to the database.
 * Internal → Spring creates a proxy around this class. Before each method call,
 *             the proxy opens a transaction. After the method returns, it commits.
 *             If a RuntimeException is thrown, it rolls back automatically.
 *
 * readOnly = true on individual read methods:
 * What    → Tells Spring/Hibernate this transaction will not modify data.
 * Why     → Hibernate skips dirty checking (no need to track changes),
 *            which improves performance for read-only operations.
 *            Some databases also optimize read-only transactions.
 */
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    /*
     * CONSTRUCTOR INJECTION — not field injection (@Autowired on fields).
     *
     * Why constructor injection?
     * 1. Dependencies are explicit — you can see exactly what this class needs.
     * 2. Immutability — fields are final, cannot be accidentally reassigned.
     * 3. Testability — you can instantiate this class in tests without Spring:
     *      new EmployeeServiceImpl(mockRepository, mockMapper)
     * 4. Fail-fast — if a dependency is missing, the app fails at startup,
     *    not at runtime when the method is first called.
     *
     * Spring automatically injects the beans because there is only one
     * constructor. No @Autowired annotation needed (Spring 4.3+).
     */
    public EmployeeServiceImpl(EmployeeRepository employeeRepository,
                                EmployeeMapper employeeMapper) {
        this.employeeRepository = employeeRepository;
        this.employeeMapper = employeeMapper;
    }

    @Override
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        log.info("Creating new employee with email: {}", request.getEmail());

        // Business rule: no two employees can share the same email address.
        // We check at the service layer (not just relying on the DB unique constraint)
        // so we can return a clean 409 CONFLICT response instead of a DB exception.
        if (employeeRepository.existsByEmail(request.getEmail())) {
            log.warn("Duplicate email attempt: {}", request.getEmail());
            throw new DuplicateEmailException(request.getEmail());
        }

        Employee employee = employeeMapper.toEntity(request);
        Employee savedEmployee = employeeRepository.save(employee);

        log.info("Employee created successfully with ID: {}", savedEmployee.getId());
        return employeeMapper.toResponse(savedEmployee);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAllEmployees() {
        log.info("Fetching all employees");

        return employeeRepository.findAll()
                .stream()
                .map(employeeMapper::toResponse)
                .toList();
        /*
         * .stream()              → converts List<Employee> to a Stream
         * .map(mapper::toResponse) → transforms each Employee to EmployeeResponse
         *                            (method reference, equivalent to e -> mapper.toResponse(e))
         * .toList()              → collects back into an unmodifiable List (Java 16+)
         */
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long id) {
        log.info("Fetching employee with ID: {}", id);

        /*
         * findById() returns Optional<Employee>.
         * orElseThrow() unwraps the value if present, or throws the supplied exception.
         * This is the idiomatic Java way to handle "not found" — no null checks needed.
         */
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Employee not found with ID: {}", id);
                    return new EmployeeNotFoundException(id);
                });

        return employeeMapper.toResponse(employee);
    }

    @Override
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        log.info("Updating employee with ID: {}", id);

        // First verify the employee exists — throw 404 if not
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Employee not found for update with ID: {}", id);
                    return new EmployeeNotFoundException(id);
                });

        // Check if the new email is already taken by a DIFFERENT employee.
        // existsByEmailAndIdNot excludes the current employee from the check,
        // so updating other fields while keeping the same email is allowed.
        if (employeeRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            log.warn("Duplicate email on update: {}", request.getEmail());
            throw new DuplicateEmailException(request.getEmail());
        }

        // Mutate the existing managed entity — Hibernate dirty checking
        // will detect the changes and issue an UPDATE on transaction commit.
        employeeMapper.updateEntityFromRequest(request, employee);

        Employee updatedEmployee = employeeRepository.save(employee);
        log.info("Employee updated successfully with ID: {}", id);
        return employeeMapper.toResponse(updatedEmployee);
    }

    @Override
    public void deleteEmployee(Long id) {
        log.info("Deleting employee with ID: {}", id);

        // Verify existence before deleting.
        // deleteById() silently does nothing if the id doesn't exist,
        // which would give the client a misleading 204 success response.
        // We want to return 404 if the employee doesn't exist.
        if (!employeeRepository.existsById(id)) {
            log.warn("Employee not found for deletion with ID: {}", id);
            throw new EmployeeNotFoundException(id);
        }

        employeeRepository.deleteById(id);
        log.info("Employee deleted successfully with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> searchEmployees(String keyword) {
        log.info("Searching employees with keyword: {}", keyword);

        return employeeRepository.searchEmployees(keyword)
                .stream()
                .map(employeeMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getEmployeesByDepartment(String department) {
        log.info("Fetching employees in department: {}", department);

        return employeeRepository.findByDepartment(department)
                .stream()
                .map(employeeMapper::toResponse)
                .toList();
    }
}
