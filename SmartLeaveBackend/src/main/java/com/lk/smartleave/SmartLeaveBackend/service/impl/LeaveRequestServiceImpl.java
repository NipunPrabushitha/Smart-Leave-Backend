package com.lk.smartleave.SmartLeaveBackend.service.impl;

import com.lk.smartleave.SmartLeaveBackend.dto.LeaveRequestDTO;
import com.lk.smartleave.SmartLeaveBackend.dto.ResponseDTO;
import com.lk.smartleave.SmartLeaveBackend.entity.Employee;
import com.lk.smartleave.SmartLeaveBackend.entity.EmployeeStatus;
import com.lk.smartleave.SmartLeaveBackend.entity.LeaveRequest;
import com.lk.smartleave.SmartLeaveBackend.entity.LeaveStatus;
import com.lk.smartleave.SmartLeaveBackend.entity.LeaveType;
import com.lk.smartleave.SmartLeaveBackend.repo.EmployeeRepository;
import com.lk.smartleave.SmartLeaveBackend.repo.LeaveRequestRepository;
import com.lk.smartleave.SmartLeaveBackend.service.LeaveRequestService;
import com.lk.smartleave.SmartLeaveBackend.util.VarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional // Add class-level transactional
public class LeaveRequestServiceImpl implements LeaveRequestService {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    @Transactional // Ensure transaction for save
    public ResponseDTO saveLeaveRequest(LeaveRequestDTO leaveRequestDTO) {
        try {
            // Find employee by email
            Optional<Employee> employee = employeeRepository.findByEmail(leaveRequestDTO.getEmployeeEmail());
            if (employee.isEmpty()) {
                return new ResponseDTO(VarList.Not_Found, "Employee not found", null);
            }

            // Check if employee is ACTIVE
            Employee emp = employee.get();
            if (!"ACTIVE".equals(emp.getStatus().toString())) {
                return new ResponseDTO(VarList.Not_Acceptable, "Employee is not active", null);
            }

            // Create and save leave request
            LeaveRequest leaveRequest = new LeaveRequest();
            leaveRequest.setEmployee(emp);
            leaveRequest.setLeaveType(LeaveType.valueOf(leaveRequestDTO.getLeaveType()));
            leaveRequest.setStartDate(leaveRequestDTO.getStartDate());
            leaveRequest.setEndDate(leaveRequestDTO.getEndDate());
            leaveRequest.setReason(leaveRequestDTO.getReason());
            leaveRequest.setStatus(LeaveStatus.PENDING);
            leaveRequest.setAppliedDate(LocalDate.now());

            LeaveRequest savedRequest = leaveRequestRepository.save(leaveRequest);

            // Convert saved entity to DTO with all fields
            LeaveRequestDTO savedDTO = convertToDTO(savedRequest);

            return new ResponseDTO(VarList.Created, "Leave request created successfully", savedDTO);

        } catch (Exception e) {

            return new ResponseDTO(VarList.Internal_Server_Error, e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public LeaveRequestDTO updateLeaveRequest(LeaveRequestDTO leaveRequestDTO) {
        Optional<LeaveRequest> existingRequest = leaveRequestRepository.findById(leaveRequestDTO.getId());
        if (existingRequest.isPresent()) {
            LeaveRequest request = existingRequest.get();
            request.setLeaveType(LeaveType.valueOf(leaveRequestDTO.getLeaveType()));
            request.setStartDate(leaveRequestDTO.getStartDate());
            request.setEndDate(leaveRequestDTO.getEndDate());
            request.setReason(leaveRequestDTO.getReason());

            // Only allow status update if it's provided
            if (leaveRequestDTO.getStatus() != null) {
                request.setStatus(LeaveStatus.valueOf(leaveRequestDTO.getStatus()));
            }

            LeaveRequest saved = leaveRequestRepository.save(request);
            return convertToDTO(saved);
        }
        return null;
    }

    @Override
    @Transactional
    public int deleteLeaveRequest(int id) {
        Optional<LeaveRequest> leaveRequest = leaveRequestRepository.findById(id);
        if (leaveRequest.isPresent()) {
            leaveRequestRepository.deleteById(id);
            return VarList.OK;
        }
        return VarList.Not_Found;
    }


    @Override
    @Transactional(readOnly = true) // Read-only transaction for queries
    public List<LeaveRequestDTO> getAllLeaveRequests() {
        List<LeaveRequest> leaveRequests = leaveRequestRepository.findAllOrderByAppliedDateDesc();
        return convertToDTOList(leaveRequests);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequestDTO> getLeaveRequestsByEmployee(int employeeId) {
        List<LeaveRequest> leaveRequests = leaveRequestRepository.findByEmployeeId(employeeId);
        return convertToDTOList(leaveRequests);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequestDTO> getLeaveRequestsByEmployeeEmail(String email) {
        List<LeaveRequest> leaveRequests = leaveRequestRepository.findByEmployeeEmail(email);
        return convertToDTOList(leaveRequests);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequestDTO> getLeaveRequestsByStatus(String status) {
        List<LeaveRequest> leaveRequests = leaveRequestRepository.findByStatus(LeaveStatus.valueOf(status.toUpperCase()));
        return convertToDTOList(leaveRequests);
    }

    @Override
    @Transactional(readOnly = true)
    public LeaveRequestDTO getLeaveRequestById(int id) {
        Optional<LeaveRequest> leaveRequest = leaveRequestRepository.findById(id);
        return leaveRequest.map(this::convertToDTO).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isLeaveRequestOwnedByUser(int leaveId, String userEmail) {
        Optional<LeaveRequest> leaveRequest = leaveRequestRepository.findById(leaveId);
        if (leaveRequest.isPresent()) {
            LeaveRequest request = leaveRequest.get();
            return request.getEmployee() != null &&
                    request.getEmployee().getUser() != null &&
                    userEmail.equals(request.getEmployee().getUser().getEmail());
        }
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequestDTO> getLeaveRequestsByUserEmail(String userEmail) {
        List<LeaveRequest> leaveRequests = leaveRequestRepository.findByEmployeeEmail(userEmail);
        return convertToDTOList(leaveRequests);
    }

    // Helper method to convert single entity to DTO
    private LeaveRequestDTO convertToDTO(LeaveRequest leaveRequest) {
        if (leaveRequest == null) {
            return null;
        }

        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setId(leaveRequest.getId());

        // Eagerly fetch the employee relationship within transaction
        if (leaveRequest.getEmployee() != null) {
            Employee employee = leaveRequest.getEmployee();
            dto.setEmployeeId(employee.getId());
            dto.setEmployeeName(employee.getName());
            dto.setEmployeeEmail(employee.getEmail());
        }

        if (leaveRequest.getLeaveType() != null) {
            dto.setLeaveType(leaveRequest.getLeaveType().name());
        }

        dto.setStartDate(leaveRequest.getStartDate());
        dto.setEndDate(leaveRequest.getEndDate());
        dto.setReason(leaveRequest.getReason());

        if (leaveRequest.getStatus() != null) {
            dto.setStatus(leaveRequest.getStatus().name());
        }

        dto.setAppliedDate(leaveRequest.getAppliedDate());
        return dto;
    }

    // Helper method to convert list of entities to DTOs
    private List<LeaveRequestDTO> convertToDTOList(List<LeaveRequest> leaveRequests) {
        return leaveRequests.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    @Override
    @Transactional
    public int updateLeaveStatus(int leaveId, String status) {
        try {
            Optional<LeaveRequest> leaveRequest = leaveRequestRepository.findById(leaveId);
            if (leaveRequest.isEmpty()) {
                return VarList.Not_Found; // Leave request not found
            }

            // Validate status
            if (!status.equalsIgnoreCase("APPROVED") && !status.equalsIgnoreCase("REJECTED")) {
                return VarList.Bad_Request; // Invalid status
            }

            LeaveRequest request = leaveRequest.get();
            request.setStatus(LeaveStatus.valueOf(status.toUpperCase()));
            leaveRequestRepository.save(request);

            return VarList.OK; // Success
        } catch (IllegalArgumentException e) {
            return VarList.Bad_Request; // Invalid status value
        } catch (Exception e) {
            return VarList.Internal_Server_Error; // Other errors
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> findEmployeesWithMoreThan5LeavesInLast30Days() {
        try {
            return leaveRequestRepository.findEmployeesWithMoreThan5LeavesInLast30Days();
        } catch (Exception e) {
            // Log the error and return empty list
            System.err.println("Error fetching frequent leave takers: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}





/*
!"ACTIVE".equals(emp.getStatus().toString())*/
