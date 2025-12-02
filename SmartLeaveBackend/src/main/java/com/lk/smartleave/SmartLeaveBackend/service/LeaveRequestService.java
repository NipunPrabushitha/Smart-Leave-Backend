package com.lk.smartleave.SmartLeaveBackend.service;

import com.lk.smartleave.SmartLeaveBackend.dto.LeavePaginationResponseDTO;
import com.lk.smartleave.SmartLeaveBackend.dto.LeaveRequestDTO;
import com.lk.smartleave.SmartLeaveBackend.dto.PaginationRequestDTO;
import com.lk.smartleave.SmartLeaveBackend.dto.ResponseDTO;
import com.lk.smartleave.SmartLeaveBackend.entity.LeaveStatus;

import java.util.List;

public interface LeaveRequestService {
    // Existing methods
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

    // NEW PAGINATION METHODS
    LeavePaginationResponseDTO getAllLeaveRequestsPaginated(PaginationRequestDTO paginationRequest);
    LeavePaginationResponseDTO getLeaveRequestsByEmployeePaginated(int employeeId, PaginationRequestDTO paginationRequest);
    LeavePaginationResponseDTO getLeaveRequestsByStatusPaginated(String status, PaginationRequestDTO paginationRequest);
    LeavePaginationResponseDTO getLeaveRequestsByUserEmailPaginated(String userEmail, PaginationRequestDTO paginationRequest);

    // Advanced filtering (for admin)
    LeavePaginationResponseDTO filterLeaveRequests(
            Integer employeeId,
            String status,
            String leaveType,
            String employeeEmail,
            PaginationRequestDTO paginationRequest
    );
}