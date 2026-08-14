package com.example.employeemanagement.service;

import com.example.employeemanagement.dto.EmployeeRequest;
import com.example.employeemanagement.dto.EmployeeResponse;

import java.util.List;

/*
 * WHY AN INTERFACE + IMPLEMENTATION INSTEAD OF JUST ONE CLASS?
 *
 * 1. Abstraction / Loose coupling:
 *    The controller depends on EmployeeService (the interface), not on
 *    EmployeeServiceImpl (the concrete class). This means you can swap
 *    implementations without touching the controller.
 *
 * 2. Testability:
 *    In unit tests, you can mock EmployeeService without needing a real
 *    database. Mockito works by creating a proxy of the interface.
 *
 * 3. SOLID - Dependency Inversion Principle:
 *    High-level modules (Controller) should depend on abstractions (Service interface),
 *    not on concrete implementations (ServiceImpl).
 *
 * This interface defines the CONTRACT — what operations are available.
 * EmployeeServiceImpl defines the IMPLEMENTATION — how they work.
 */
public interface EmployeeService {

    EmployeeResponse createEmployee(EmployeeRequest request);

    List<EmployeeResponse> getAllEmployees();

    EmployeeResponse getEmployeeById(Long id);

    EmployeeResponse updateEmployee(Long id, EmployeeRequest request);

    void deleteEmployee(Long id);

    List<EmployeeResponse> searchEmployees(String keyword);

    List<EmployeeResponse> getEmployeesByDepartment(String department);
}
