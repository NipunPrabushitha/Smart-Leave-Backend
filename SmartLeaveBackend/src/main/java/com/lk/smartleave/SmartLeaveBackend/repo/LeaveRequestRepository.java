package com.lk.smartleave.SmartLeaveBackend.repo;

import com.lk.smartleave.SmartLeaveBackend.entity.LeaveRequest;
import com.lk.smartleave.SmartLeaveBackend.entity.LeaveStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Integer> {

    // Existing methods
    List<LeaveRequest> findByEmployeeId(int employeeId);
    List<LeaveRequest> findByStatus(LeaveStatus status);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.email = :email")
    List<LeaveRequest> findByEmployeeEmail(@Param("email") String email);

    @Query("SELECT lr FROM LeaveRequest lr ORDER BY lr.appliedDate DESC")
    List<LeaveRequest> findAllOrderByAppliedDateDesc();

    // NEW PAGINATION METHODS
    Page<LeaveRequest> findAll(Pageable pageable);

    Page<LeaveRequest> findByEmployeeId(int employeeId, Pageable pageable);

    Page<LeaveRequest> findByStatus(LeaveStatus status, Pageable pageable);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.email = :email")
    Page<LeaveRequest> findByEmployeeEmail(@Param("email") String email, Pageable pageable);

    @Query("SELECT lr FROM LeaveRequest lr ORDER BY lr.appliedDate DESC")
    Page<LeaveRequest> findAllOrderByAppliedDateDesc(Pageable pageable);

    // Filter with multiple criteria (for admin)
    @Query("SELECT lr FROM LeaveRequest lr WHERE " +
            "(:employeeId IS NULL OR lr.employee.id = :employeeId) AND " +
            "(:status IS NULL OR lr.status = :status) AND " +
            "(:leaveType IS NULL OR lr.leaveType = :leaveType) AND " +
            "(:employeeEmail IS NULL OR lr.employee.email LIKE %:employeeEmail%)")
    Page<LeaveRequest> findWithFilters(
            @Param("employeeId") Integer employeeId,
            @Param("status") LeaveStatus status,
            @Param("leaveType") String leaveType,
            @Param("employeeEmail") String employeeEmail,
            Pageable pageable
    );

    // Filter by date range (for admin analytics)
    @Query("SELECT lr FROM LeaveRequest lr WHERE " +
            "lr.startDate >= :startDate AND " +
            "lr.endDate <= :endDate")
    Page<LeaveRequest> findByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    // Existing analytics query
    @Query(value = "SELECT e.id, e.name, e.email, e.department, " +
            "COUNT(lr.id) as leave_count " +
            "FROM employees e " +
            "INNER JOIN leave_requests lr ON e.id = lr.employee_id " +
            "WHERE lr.start_date >= DATE_SUB(CURRENT_DATE, INTERVAL 30 DAY) " +
            "AND lr.status IN ('APPROVED', 'PENDING') " +
            "GROUP BY e.id, e.name, e.email, e.department " +
            "HAVING COUNT(lr.id) > 5",
            nativeQuery = true)
    List<Object[]> findEmployeesWithMoreThan5LeavesInLast30Days();
}