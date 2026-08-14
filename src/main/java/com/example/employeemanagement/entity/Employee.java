package com.example.employeemanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/*
 * WHY LOMBOK ANNOTATIONS HERE:
 *
 * @Getter        → generates getGetter() for every field
 * @Setter        → generates setSetter() for every field
 * @NoArgsConstructor → generates Employee() — required by JPA/Hibernate.
 *                      Hibernate needs a no-arg constructor to instantiate
 *                      objects when loading data from the database via reflection.
 * @AllArgsConstructor → generates Employee(id, firstName, ...) — useful in tests
 * @Builder       → generates Employee.builder().firstName("John").build()
 *                  Makes object construction readable without telescoping constructors.
 *
 * We use @Getter/@Setter separately instead of @Data because @Data also generates
 * equals/hashCode based on all fields, which causes problems with JPA proxies and
 * bidirectional relationships. Explicit control is safer for entities.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

/*
 * @Entity:
 * What    → Marks this class as a JPA-managed entity.
 * Why     → Without this, Hibernate ignores the class entirely.
 * Internal → Hibernate registers it in the EntityManagerFactory's metamodel.
 * Where   → Only on classes that represent database tables.
 */
@Entity

/*
 * @Table:
 * What    → Specifies the exact table name and constraints in the database.
 * Why     → Without it, Hibernate uses the class name as the table name ("Employee").
 *            Explicit naming avoids surprises and follows SQL naming conventions.
 * uniqueConstraints → Hibernate adds a UNIQUE INDEX on the email column at the DB level.
 *                     This is a second layer of protection beyond our service-layer check.
 */
@Table(
    name = "employees",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_employee_email",
        columnNames = "email"
    )
)
public class Employee {

    /*
     * @Id:
     * What    → Marks this field as the primary key of the table.
     * Why     → Every JPA entity must have exactly one @Id field.
     * Internal → Hibernate uses this to track entity identity and manage the persistence context.
     *
     * @GeneratedValue(strategy = GenerationType.IDENTITY):
     * What    → Tells Hibernate to let the database generate the ID value.
     * Why     → IDENTITY strategy uses MySQL's AUTO_INCREMENT.
     *            When you INSERT a row, MySQL assigns the next available ID automatically.
     * Alternatives:
     *   SEQUENCE  → uses a DB sequence (better for PostgreSQL)
     *   TABLE     → uses a separate table to track IDs (portable but slow)
     *   AUTO      → Hibernate picks a strategy (unpredictable, avoid)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * @Column:
     * What    → Maps this field to a specific database column with constraints.
     * Why     → Without it, Hibernate uses the field name as the column name.
     *            Explicit mapping gives you control over name, nullability, and length.
     *
     * name         → the actual column name in MySQL
     * nullable     → adds NOT NULL constraint at the DB level
     * length       → sets VARCHAR length (default is 255 if omitted)
     *
     * WHY String for names?
     * Names are text. String maps to VARCHAR in SQL.
     * length=100 is enough for a name and avoids wasting storage.
     */
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    /*
     * WHY unique = true here AND uniqueConstraints in @Table?
     * unique=true on @Column creates an unnamed unique constraint.
     * uniqueConstraints in @Table creates a named constraint (uk_employee_email).
     * Named constraints give better error messages and are easier to manage in migrations.
     * We use @Table's uniqueConstraints and skip unique=true here to avoid duplication.
     */
    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "department", nullable = false, length = 100)
    private String department;

    @Column(name = "job_title", nullable = false, length = 100)
    private String jobTitle;

    /*
     * WHY BigDecimal for salary?
     * float and double use binary floating-point arithmetic.
     * Binary floating-point CANNOT represent most decimal fractions exactly.
     * Example: 0.1 + 0.2 = 0.30000000000000004 in double.
     * For money, this is unacceptable. BigDecimal stores exact decimal values.
     *
     * precision=15 → total number of significant digits
     * scale=2      → digits after the decimal point (e.g., 75000.00)
     * Maps to DECIMAL(15,2) in MySQL.
     */
    @Column(name = "salary", precision = 15, scale = 2)
    private BigDecimal salary;

    /*
     * WHY LocalDate for hireDate?
     * LocalDate represents a date without time or timezone (year-month-day).
     * A hire date is just a date, not a moment in time, so LocalDate is correct.
     * LocalDateTime would be wrong here — we don't care about the hour they were hired.
     * Maps to DATE in MySQL.
     */
    @Column(name = "hire_date")
    private LocalDate hireDate;

    /*
     * @CreationTimestamp:
     * What    → Hibernate automatically sets this field to the current timestamp
     *            when the entity is first persisted (INSERT).
     * Why     → Audit trail. You always want to know when a record was created.
     * Internal → Hibernate intercepts the INSERT and injects the current time.
     *             You never set this manually.
     * updatable=false → prevents this column from being changed on UPDATE.
     *                   Once set, it never changes.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /*
     * @UpdateTimestamp:
     * What    → Hibernate automatically updates this field to the current timestamp
     *            on every UPDATE operation.
     * Why     → Audit trail. You always want to know when a record was last modified.
     * Internal → Hibernate intercepts every UPDATE and injects the current time.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
