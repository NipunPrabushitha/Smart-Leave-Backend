package com.lk.smartleave.SmartLeaveBackend.service;

import com.lk.smartleave.SmartLeaveBackend.dto.LeaveRequestDTO;
import com.lk.smartleave.SmartLeaveBackend.dto.ResponseDTO;

import java.util.List;

public interface LeaveRequestService {
    ResponseDTO saveLeaveRequest(LeaveRequestDTO leaveRequestDTO);
    LeaveRequestDTO updateLeaveRequest(LeaveRequestDTO leaveRequestDTO);
    int deleteLeaveRequest(int id);
    List<LeaveRequestDTO> getAllLeaveRequests();
    List<LeaveRequestDTO> getLeaveRequestsByEmployee(int employeeId);
    List<LeaveRequestDTO> getLeaveRequestsByEmployeeEmail(String email);
    List<LeaveRequestDTO> getLeaveRequestsByStatus(String status);
    LeaveRequestDTO getLeaveRequestById(int id);
    boolean isLeaveRequestOwnedByUser(int leaveId, String userEmail);
    List<LeaveRequestDTO> getLeaveRequestsByUserEmail(String userEmail);
    int updateLeaveStatus(int leaveId, String status);
    List<Object[]> findEmployeesWithMoreThan5LeavesInLast30Days();
}