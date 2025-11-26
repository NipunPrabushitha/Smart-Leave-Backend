package com.lk.smartleave.SmartLeaveBackend.repo;

import com.lk.smartleave.SmartLeaveBackend.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    Optional<Employee> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("SELECT e FROM Employee e WHERE e.name LIKE %:name%")
    List<Employee> findByNameContaining(@Param("name") String name);

    @Query("SELECT e FROM Employee e WHERE e.department = :department")
    List<Employee> findByDepartment(@Param("department") String department);

    @Query("SELECT e FROM Employee e WHERE e.name LIKE %:name% AND e.department = :department")
    List<Employee> findByNameAndDepartment(@Param("name") String name, @Param("department") String department);

    Optional<Employee> findByUserEmail(String email);
}