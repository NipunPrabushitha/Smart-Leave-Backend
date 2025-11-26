package com.lk.smartleave.SmartLeaveBackend.controller;

import com.lk.smartleave.SmartLeaveBackend.dto.LeaveRequestDTO;
import com.lk.smartleave.SmartLeaveBackend.dto.ResponseDTO;
import com.lk.smartleave.SmartLeaveBackend.service.EmployeeService;
import com.lk.smartleave.SmartLeaveBackend.service.LeaveRequestService;
import com.lk.smartleave.SmartLeaveBackend.util.VarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("api/v1/employee")
@PreAuthorize("hasAnyAuthority('EMPLOYEE', 'ADMIN')")
public class EmployeeController {

    @Autowired
    private LeaveRequestService leaveRequestService;

    @Autowired
    private EmployeeService employeeService;

    // Leave Management

    @PostMapping("/leave-requests")
    public ResponseEntity<ResponseDTO> applyForLeave(@RequestBody LeaveRequestDTO leaveRequestDTO) {
        try {
            int res = leaveRequestService.applyForLeave(leaveRequestDTO);

            if (res == VarList.Created) {
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(new ResponseDTO(VarList.Created, "Leave application submitted successfully", null));
            }

            if (res == VarList.Not_Found) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseDTO(VarList.Not_Found, "Employee not found", null));
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseDTO(VarList.Bad_Gateway, "Error submitting leave application", null));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO(VarList.Internal_Server_Error, e.getMessage(), null));
        }
    }

    @GetMapping("/leave-requests/my-leaves")
    public ResponseEntity<ResponseDTO> getMyLeaveRequests(@RequestParam String email) {
        try {
            List<LeaveRequestDTO> leaveRequests = leaveRequestService.getLeaveRequestsByEmployeeEmail(email);
            return ResponseEntity.ok(
                    new ResponseDTO(VarList.OK, "Success", leaveRequests)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO(VarList.Internal_Server_Error, e.getMessage(), null));
        }
    }

    @GetMapping("/leave-requests/{id}")
    public ResponseEntity<ResponseDTO> getLeaveRequestById(@PathVariable int id) {
        try {
            LeaveRequestDTO leaveRequest = leaveRequestService.getLeaveRequestById(id);

            if (leaveRequest == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseDTO(VarList.Not_Found, "Leave request not found", null));
            }

            return ResponseEntity.ok(
                    new ResponseDTO(VarList.OK, "Success", leaveRequest)
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO(VarList.Internal_Server_Error, e.getMessage(), null));
        }
    }

    // Employee profile

    @GetMapping("/profile/{email}")
    public ResponseEntity<ResponseDTO> getEmployeeProfile(@PathVariable String email) {
        try {
            var employee = employeeService.getEmployeeByEmail(email);

            if (employee == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseDTO(VarList.Not_Found, "Employee not found", null));
            }

            return ResponseEntity.ok(
                    new ResponseDTO(VarList.OK, "Success", employee)
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO(VarList.Internal_Server_Error, e.getMessage(), null));
        }
    }
}