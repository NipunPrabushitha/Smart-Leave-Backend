package com.lk.smartleave.SmartLeaveBackend.service.impl;

import com.lk.smartleave.SmartLeaveBackend.dto.LeavePaginationResponseDTO;
import com.lk.smartleave.SmartLeaveBackend.dto.LeaveRequestDTO;
import com.lk.smartleave.SmartLeaveBackend.dto.PaginationRequestDTO;
import com.lk.smartleave.SmartLeaveBackend.dto.ResponseDTO;
import com.lk.smartleave.SmartLeaveBackend.entity.Employee;
import com.lk.smartleave.SmartLeaveBackend.entity.LeaveRequest;
import com.lk.smartleave.SmartLeaveBackend.entity.LeaveStatus;
import com.lk.smartleave.SmartLeaveBackend.entity.LeaveType;
import com.lk.smartleave.SmartLeaveBackend.repo.EmployeeRepository;
import com.lk.smartleave.SmartLeaveBackend.repo.LeaveRequestRepository;
import com.lk.smartleave.SmartLeaveBackend.service.LeaveRequestService;
import com.lk.smartleave.SmartLeaveBackend.util.VarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class LeaveRequestServiceImpl implements LeaveRequestService {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    // ========== EXISTING METHODS (UNCHANGED) ==========
    @Override
    @Transactional
    public ResponseDTO saveLeaveRequest(LeaveRequestDTO leaveRequestDTO) {
        try {
            Optional<Employee> employee = employeeRepository.findByEmail(leaveRequestDTO.getEmployeeEmail());
            if (employee.isEmpty()) {
                return new ResponseDTO(VarList.Not_Found, "Employee not found", null);
            }

            Employee emp = employee.get();
            if (!"ACTIVE".equals(emp.getStatus().toString())) {
                return new ResponseDTO(VarList.Not_Acceptable, "Employee is not active", null);
            }

            LeaveRequest leaveRequest = new LeaveRequest();
            leaveRequest.setEmployee(emp);
            leaveRequest.setLeaveType(LeaveType.valueOf(leaveRequestDTO.getLeaveType()));
            leaveRequest.setStartDate(leaveRequestDTO.getStartDate());
            leaveRequest.setEndDate(leaveRequestDTO.getEndDate());
            leaveRequest.setReason(leaveRequestDTO.getReason());
            leaveRequest.setStatus(LeaveStatus.PENDING);
            leaveRequest.setAppliedDate(LocalDate.now());

            LeaveRequest savedRequest = leaveRequestRepository.save(leaveRequest);
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
    @Transactional(readOnly = true)
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

    @Override
    @Transactional
    public int updateLeaveStatus(int leaveId, String status) {
        try {
            Optional<LeaveRequest> leaveRequest = leaveRequestRepository.findById(leaveId);
            if (leaveRequest.isEmpty()) {
                return VarList.Not_Found;
            }

            if (!status.equalsIgnoreCase("APPROVED") && !status.equalsIgnoreCase("REJECTED")) {
                return VarList.Bad_Request;
            }

            LeaveRequest request = leaveRequest.get();
            request.setStatus(LeaveStatus.valueOf(status.toUpperCase()));
            leaveRequestRepository.save(request);

            return VarList.OK;
        } catch (IllegalArgumentException e) {
            return VarList.Bad_Request;
        } catch (Exception e) {
            return VarList.Internal_Server_Error;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> findEmployeesWithMoreThan5LeavesInLast30Days() {
        try {
            return leaveRequestRepository.findEmployeesWithMoreThan5LeavesInLast30Days();
        } catch (Exception e) {
            System.err.println("Error fetching frequent leave takers: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ========== NEW PAGINATION METHODS ==========

    @Override
    @Transactional(readOnly = true)
    public LeavePaginationResponseDTO getAllLeaveRequestsPaginated(PaginationRequestDTO paginationRequest) {
        Pageable pageable = createPageable(paginationRequest);
        Page<LeaveRequest> page = leaveRequestRepository.findAllOrderByAppliedDateDesc(pageable);
        return convertPageToResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public LeavePaginationResponseDTO getLeaveRequestsByEmployeePaginated(int employeeId, PaginationRequestDTO paginationRequest) {
        Pageable pageable = createPageable(paginationRequest);
        Page<LeaveRequest> page = leaveRequestRepository.findByEmployeeId(employeeId, pageable);
        return convertPageToResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public LeavePaginationResponseDTO getLeaveRequestsByStatusPaginated(String status, PaginationRequestDTO paginationRequest) {
        Pageable pageable = createPageable(paginationRequest);
        Page<LeaveRequest> page = leaveRequestRepository.findByStatus(
                LeaveStatus.valueOf(status.toUpperCase()),
                pageable
        );
        return convertPageToResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public LeavePaginationResponseDTO getLeaveRequestsByUserEmailPaginated(String userEmail, PaginationRequestDTO paginationRequest) {
        Pageable pageable = createPageable(paginationRequest);
        Page<LeaveRequest> page = leaveRequestRepository.findByEmployeeEmail(userEmail, pageable);
        return convertPageToResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public LeavePaginationResponseDTO filterLeaveRequests(
            Integer employeeId,
            String status,
            String leaveType,
            String employeeEmail,
            PaginationRequestDTO paginationRequest) {

        Pageable pageable = createPageable(paginationRequest);
        LeaveStatus leaveStatus = null;

        if (status != null && !status.isEmpty()) {
            try {
                leaveStatus = LeaveStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // If invalid status, ignore it
            }
        }

        Page<LeaveRequest> page = leaveRequestRepository.findWithFilters(
                employeeId,
                leaveStatus,
                leaveType,
                employeeEmail,
                pageable
        );

        return convertPageToResponse(page);
    }

    // ========== HELPER METHODS ==========

    private Pageable createPageable(PaginationRequestDTO paginationRequest) {
        Sort.Direction direction = paginationRequest.isAscending()
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        // Validate sort field to prevent SQL injection
        String sortField = paginationRequest.getSortBy();
        List<String> validSortFields = List.of("id", "startDate", "endDate", "appliedDate", "status");

        if (!validSortFields.contains(sortField)) {
            sortField = "id"; // Default to id if invalid field
        }

        Sort sort = Sort.by(direction, sortField);

        return PageRequest.of(
                paginationRequest.getPage(),
                paginationRequest.getSize(),
                sort
        );
    }

    private LeavePaginationResponseDTO convertPageToResponse(Page<LeaveRequest> page) {
        List<LeaveRequestDTO> content = convertToDTOList(page.getContent());

        return new LeavePaginationResponseDTO(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious()
        );
    }

    // Existing helper methods (keep these)
    private LeaveRequestDTO convertToDTO(LeaveRequest leaveRequest) {
        if (leaveRequest == null) {
            return null;
        }

        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setId(leaveRequest.getId());

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

    private List<LeaveRequestDTO> convertToDTOList(List<LeaveRequest> leaveRequests) {
        return leaveRequests.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}