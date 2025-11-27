package com.lk.smartleave.SmartLeaveBackend.controller;

import com.lk.smartleave.SmartLeaveBackend.dto.LeaveRequestDTO;
import com.lk.smartleave.SmartLeaveBackend.dto.ResponseDTO;
import com.lk.smartleave.SmartLeaveBackend.service.LeaveRequestService;
import com.lk.smartleave.SmartLeaveBackend.util.VarList;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin("*")
@RequestMapping("api/v1/leave-request")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    public LeaveRequestController(LeaveRequestService leaveRequestService) {
        this.leaveRequestService = leaveRequestService;
    }

    @PostMapping("/create")
    public ResponseEntity<ResponseDTO> createLeaveRequest(
            @Valid @RequestBody LeaveRequestDTO leaveRequestDTO,
            HttpServletRequest request) {
        try {
            String tokenEmail = (String) request.getAttribute("email");

            if (tokenEmail == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ResponseDTO(VarList.Unauthorized, "Token required", null));
            }

            // Set the employee email from token
            leaveRequestDTO.setEmployeeEmail(tokenEmail);

            ResponseDTO response = leaveRequestService.saveLeaveRequest(leaveRequestDTO);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO(VarList.Internal_Server_Error, e.getMessage(), null));
        }
    }

    // GET MY LEAVE REQUESTS (requires JWT and user-specific validation)
    @GetMapping("/my-requests")
    public ResponseEntity<ResponseDTO> getMyLeaveRequests(HttpServletRequest request) {
        try {
            // Get user email from token
            String userEmail = (String) request.getAttribute("email");

            List<LeaveRequestDTO> leaveRequests = leaveRequestService.getLeaveRequestsByUserEmail(userEmail);
            return ResponseEntity.ok(
                    new ResponseDTO(VarList.OK, "Success", leaveRequests)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO(VarList.Internal_Server_Error, e.getMessage(), null));
        }
    }

    // UPDATE LEAVE REQUEST (requires JWT and user-specific validation)
    @PutMapping("/update")
    public ResponseEntity<ResponseDTO> updateLeaveRequest(
            @Valid @RequestBody LeaveRequestDTO leaveRequestDTO,
            HttpServletRequest request) {
        try {
            String tokenEmail = (String) request.getAttribute("email");

            if (tokenEmail == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ResponseDTO(VarList.Unauthorized, "Token required", null));
            }

            // Check if the leave request belongs to the authenticated user
            LeaveRequestDTO existingRequest = leaveRequestService.getLeaveRequestById(leaveRequestDTO.getId());
            if (existingRequest == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseDTO(VarList.Not_Found, "Leave request not found", null));
            }

            if (!existingRequest.getEmployeeEmail().equals(tokenEmail)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ResponseDTO(VarList.Forbidden, "You can only update your own leave requests", null));
            }

            // Ensure the employee email cannot be changed
            leaveRequestDTO.setEmployeeEmail(tokenEmail);

            // Users can only update leave requests that are still PENDING
            if (!"PENDING".equals(existingRequest.getStatus())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ResponseDTO(VarList.Forbidden, "You can only update pending leave requests", null));
            }

            // Ensure users cannot change status (only admin can approve/reject)
            leaveRequestDTO.setStatus("PENDING");

            LeaveRequestDTO updated = leaveRequestService.updateLeaveRequest(leaveRequestDTO);

            if (updated == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseDTO(VarList.Not_Found, "Leave request not found", null));
            }

            return ResponseEntity.ok(
                    new ResponseDTO(VarList.OK, "Updated successfully", updated)
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO(VarList.Internal_Server_Error, e.getMessage(), null));
        }
    }

    // DELETE LEAVE REQUEST (requires JWT and user-specific validation)
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ResponseDTO> deleteLeaveRequest(
            @PathVariable int id,
            HttpServletRequest request) {
        try {
            String tokenEmail = (String) request.getAttribute("email");

            if (tokenEmail == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ResponseDTO(VarList.Unauthorized, "Token required", null));
            }

            // Check if the leave request belongs to the authenticated user
            LeaveRequestDTO existingRequest = leaveRequestService.getLeaveRequestById(id);
            if (existingRequest == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseDTO(VarList.Not_Found, "Leave request not found", null));
            }

            if (!existingRequest.getEmployeeEmail().equals(tokenEmail)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ResponseDTO(VarList.Forbidden, "You can only delete your own leave requests", null));
            }

            int res = leaveRequestService.deleteLeaveRequest(id);

            if (res == VarList.OK) {
                return ResponseEntity.ok(
                        new ResponseDTO(VarList.OK, "Deleted successfully", null)
                );
            }

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO(VarList.Not_Found, "Leave request not found", null));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO(VarList.Internal_Server_Error, e.getMessage(), null));
        }
    }

    // GET ALL LEAVE REQUESTS (Admin only)
    @GetMapping("/all")
    public ResponseEntity<ResponseDTO> getAllLeaveRequests(HttpServletRequest request) {
        try {
            String role = (String) request.getAttribute("role");

            if (!"ADMIN".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ResponseDTO(VarList.Forbidden, "Admin access required", null));
            }

            return ResponseEntity.ok(
                    new ResponseDTO(VarList.OK, "Success", leaveRequestService.getAllLeaveRequests())
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO(VarList.Internal_Server_Error, e.getMessage(), null));
        }
    }

    // ACCEPT LEAVE REQUEST (Admin only)
    @PutMapping("/accept/{id}")
    public ResponseEntity<ResponseDTO> acceptLeaveRequest(
            @PathVariable int id,
            HttpServletRequest request) {
        try {
            String role = (String) request.getAttribute("role");

            if (!"ADMIN".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ResponseDTO(VarList.Forbidden, "Admin access required", null));
            }

            LeaveRequestDTO existingRequest = leaveRequestService.getLeaveRequestById(id);
            if (existingRequest == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseDTO(VarList.Not_Found, "Leave request not found", null));
            }

            // Update status to APPROVED
            existingRequest.setStatus("APPROVED");
            LeaveRequestDTO updated = leaveRequestService.updateLeaveRequest(existingRequest);

            return ResponseEntity.ok(
                    new ResponseDTO(VarList.OK, "Leave request accepted successfully", updated)
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO(VarList.Internal_Server_Error, e.getMessage(), null));
        }
    }

    // REJECT LEAVE REQUEST (Admin only)
    @PutMapping("/reject/{id}")
    public ResponseEntity<ResponseDTO> rejectLeaveRequest(
            @PathVariable int id,
            HttpServletRequest request) {
        try {
            String role = (String) request.getAttribute("role");

            if (!"ADMIN".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ResponseDTO(VarList.Forbidden, "Admin access required", null));
            }

            LeaveRequestDTO existingRequest = leaveRequestService.getLeaveRequestById(id);
            if (existingRequest == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseDTO(VarList.Not_Found, "Leave request not found", null));
            }

            // Update status to REJECTED
            existingRequest.setStatus("REJECTED");
            LeaveRequestDTO updated = leaveRequestService.updateLeaveRequest(existingRequest);

            return ResponseEntity.ok(
                    new ResponseDTO(VarList.OK, "Leave request rejected successfully", updated)
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO(VarList.Internal_Server_Error, e.getMessage(), null));
        }
    }
    @GetMapping("/analytics/frequent-leave-takers")
    public ResponseEntity<ResponseDTO> getFrequentLeaveTakers(HttpServletRequest request) {
        try {
            String role = (String) request.getAttribute("role");

            // Only allow ADMIN access to this analytics endpoint
            if (!"ADMIN".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ResponseDTO(VarList.Forbidden, "Admin access required", null));
            }

            List<Object[]> results = leaveRequestService.findEmployeesWithMoreThan5LeavesInLast30Days();

            if (results.isEmpty()) {
                return ResponseEntity.ok(
                        new ResponseDTO(VarList.OK, "No employees found with more than 5 leaves in last 30 days", new ArrayList<>())
                );
            }

            // Convert Object[] to readable format
            List<Map<String, Object>> formattedResults = new ArrayList<>();
            for (Object[] result : results) {
                Map<String, Object> employeeData = new HashMap<>();
                employeeData.put("employeeId", result[0]);
                employeeData.put("employeeName", result[1]);
                employeeData.put("employeeEmail", result[2]);
                employeeData.put("department", result[3]);
                employeeData.put("leaveCount", result[4]);
                formattedResults.add(employeeData);
            }

            return ResponseEntity.ok(
                    new ResponseDTO(VarList.OK, "Frequent leave takers analysis completed", formattedResults)
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO(VarList.Internal_Server_Error, "Error analyzing frequent leave takers: " + e.getMessage(), null));
        }
    }
}