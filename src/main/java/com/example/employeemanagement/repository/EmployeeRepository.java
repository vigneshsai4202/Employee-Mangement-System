package com.example.employeemanagement.repository;

import com.example.employeemanagement.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/*
 * @Repository:
 * What    → Marks this interface as a Spring-managed repository bean.
 * Why     → Enables Spring's exception translation: converts low-level
 *            JDBC/JPA exceptions into Spring's DataAccessException hierarchy.
 *            This means your service layer catches consistent Spring exceptions
 *            instead of vendor-specific ones like MySQLIntegrityConstraintViolationException.
 * Internal → Spring creates a proxy implementation of this interface at runtime.
 *             You never write the implementation yourself.
 * Note    → Technically optional when extending JpaRepository (Spring detects it anyway),
 *            but explicit is always better for clarity.
 *
 * JpaRepository<Employee, Long>:
 * The two type parameters are:
 *   Employee → the entity this repository manages
 *   Long     → the type of the primary key (@Id field)
 *
 * By extending JpaRepository, you get these methods for free:
 *   save(entity)           → INSERT or UPDATE
 *   findById(id)           → SELECT WHERE id = ?
 *   findAll()              → SELECT * FROM employees
 *   deleteById(id)         → DELETE WHERE id = ?
 *   existsById(id)         → SELECT COUNT(*) WHERE id = ?
 *   count()                → SELECT COUNT(*)
 *   findAll(Pageable)      → SELECT with LIMIT and OFFSET (pagination)
 * Spring Data JPA generates all SQL for these automatically.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    /*
     * Derived query: findByEmail
     *
     * Spring parses "findByEmail" and generates:
     *   SELECT * FROM employees WHERE email = ?
     *
     * Returns Optional<Employee> because the email might not exist.
     * Using Optional forces the caller to handle the "not found" case explicitly,
     * which is safer than returning null (avoids NullPointerException).
     */
    Optional<Employee> findByEmail(String email);

    /*
     * Derived query: findByDepartment
     *
     * Spring generates:
     *   SELECT * FROM employees WHERE department = ?
     *
     * Returns List<Employee> because multiple employees can be in the same department.
     * Returns an empty list (not null) if no employees are found — safe to iterate.
     */
    List<Employee> findByDepartment(String department);

    /*
     * Derived query: findByFirstNameContainingIgnoreCase
     *
     * Spring parses this as:
     *   "findBy"          → WHERE clause
     *   "FirstName"       → maps to firstName field
     *   "Containing"      → LIKE '%value%'
     *   "IgnoreCase"      → LOWER(first_name) LIKE LOWER('%value%')
     *
     * Generated SQL:
     *   SELECT * FROM employees WHERE LOWER(first_name) LIKE LOWER('%john%')
     *
     * This allows case-insensitive partial name search.
     */
    List<Employee> findByFirstNameContainingIgnoreCase(String firstName);

    /*
     * Derived query: findByLastNameContainingIgnoreCase
     *
     * Same pattern as above but for last_name column.
     *   SELECT * FROM employees WHERE LOWER(last_name) LIKE LOWER('%value%')
     */
    List<Employee> findByLastNameContainingIgnoreCase(String lastName);

    /*
     * Derived query: existsByEmail
     *
     * Spring generates:
     *   SELECT COUNT(*) > 0 FROM employees WHERE email = ?
     *
     * Returns boolean. Used in the service layer to check for duplicate emails
     * before creating or updating an employee, without loading the full entity.
     * More efficient than findByEmail() when you only need to know if it exists.
     */
    boolean existsByEmail(String email);

    /*
     * Derived query: existsByEmailAndIdNot
     *
     * Spring generates:
     *   SELECT COUNT(*) > 0 FROM employees WHERE email = ? AND id != ?
     *
     * Why do we need this?
     * When updating an employee, we need to check if the new email is already
     * taken by a DIFFERENT employee. We can't use existsByEmail() alone because
     * it would also match the employee being updated (same id, same email = valid).
     * This query excludes the current employee's own id from the check.
     */
    boolean existsByEmailAndIdNot(String email, Long id);

    /*
     * Custom JPQL query: searchEmployees
     *
     * @Query:
     * What    → Lets you write your own JPQL (or native SQL) query.
     * Why     → When the derived query name would be too long or complex,
     *            or when you need OR conditions across multiple fields.
     * JPQL vs SQL:
     *   JPQL uses entity class names and field names (Employee, e.firstName)
     *   SQL uses table names and column names (employees, first_name)
     *   Hibernate translates JPQL → SQL at runtime.
     *
     * This query searches across firstName, lastName, AND email in one call.
     * The derived query equivalent would be:
     *   findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase
     * That's unreadable. @Query is the right choice here.
     *
     * :keyword → named parameter, bound by @Param("keyword")
     * LOWER()  → case-insensitive comparison
     * CONCAT('%', :keyword, '%') → wraps keyword with % for LIKE matching
     */
    @Query("SELECT e FROM Employee e WHERE " +
           "LOWER(e.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Employee> searchEmployees(@Param("keyword") String keyword);
}
