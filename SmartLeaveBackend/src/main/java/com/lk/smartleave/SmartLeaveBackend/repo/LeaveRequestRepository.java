package com.lk.smartleave.SmartLeaveBackend.repo;

import com.lk.smartleave.SmartLeaveBackend.entity.LeaveRequest;
import com.lk.smartleave.SmartLeaveBackend.entity.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Integer> {
    List<LeaveRequest> findByEmployeeId(int employeeId);
    List<LeaveRequest> findByStatus(LeaveStatus status);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.email = :email")
    List<LeaveRequest> findByEmployeeEmail(@Param("email") String email);


    //Using native query
    /*
    @Query(value = "SELECT * FROM leave_request lr " +
            "INNER JOIN employee e ON lr.employee_id = e.id " +
            "WHERE e.email = :email", nativeQuery = true)
    List<LeaveRequest> findByEmployeeEmail(@Param("email") String email);
    */



    @Query("SELECT lr FROM LeaveRequest lr ORDER BY lr.appliedDate DESC")
    List<LeaveRequest> findAllOrderByAppliedDateDesc();
}