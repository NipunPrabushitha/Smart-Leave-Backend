package com.lk.smartleave.SmartLeaveBackend.service;

import com.lk.smartleave.SmartLeaveBackend.dto.LeaveRequestDTO;

import java.util.List;

public interface LeaveRequestService {
    int applyForLeave(LeaveRequestDTO leaveRequestDTO);
    int updateLeaveStatus(int leaveId, String status);
    List<LeaveRequestDTO> getAllLeaveRequests();
    List<LeaveRequestDTO> getLeaveRequestsByEmployee(int employeeId);
    List<LeaveRequestDTO> getLeaveRequestsByEmployeeEmail(String email);
    List<LeaveRequestDTO> getLeaveRequestsByStatus(String status);
    LeaveRequestDTO getLeaveRequestById(int id);
}