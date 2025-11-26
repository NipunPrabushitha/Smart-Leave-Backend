package com.lk.smartleave.SmartLeaveBackend.controller;

import com.lk.smartleave.SmartLeaveBackend.dto.EmployeeDTO;
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
@RequestMapping("api/v1/admin")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private LeaveRequestService leaveRequestService;

    // Employee Management

    @PostMapping("/employees")
    public ResponseEntity<ResponseDTO> createEmployee(@RequestBody EmployeeDTO employeeDTO) {
        try {
            int res = employeeService.saveEmployee(employeeDTO);

            if (res == VarList.Created) {
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(new ResponseDTO(VarList.Created, "Employee created successfully", null));
            }

            if (res == VarList.Not_Acceptable) {
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                        .body(new ResponseDTO(VarList.Not_Acceptable, "Email already exists", null));
            }

            if (res == VarList.Not_Found) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseDTO(VarList.Not_Found, "User not found for the given email", null));
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseDTO(VarList.Bad_Gateway, "Error creating employee", null));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO(VarList.Internal_Server_Error, e.getMessage(), null));
        }
    }

    @PutMapping("/employees")
    public ResponseEntity<ResponseDTO> updateEmployee(@RequestBody EmployeeDTO employeeDTO) {
        try {
            int res = employeeService.updateEmployee(employeeDTO);

            if (res == VarList.OK) {
                return ResponseEntity.ok(
                        new ResponseDTO(VarList.OK, "Employee updated successfully", null)
                );
            }

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO(VarList.Not_Found, "Employee not found", null));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO(VarList.Internal_Server_Error, e.getMessage(), null));
        }
    }

    @DeleteMapping("/employees/{id}")
    public ResponseEntity<ResponseDTO> deleteEmployee(@PathVariable int id) {
        try {
            int res = employeeService.deleteEmployee(id);

            if (res == VarList.OK) {
                return ResponseEntity.ok(
                        new ResponseDTO(VarList.OK, "Employee deleted successfully", null)
                );
            }

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO(VarList.Not_Found, "Employee not found", null));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO(VarList.Internal_Server_Error, e.getMessage(), null));
        }
    }

    @GetMapping("/employees")
    public ResponseEntity<ResponseDTO> getAllEmployees() {
        try {
            List<EmployeeDTO> employees = employeeService.getAllEmployees();
            return ResponseEntity.ok(
                    new ResponseDTO(VarList.OK, "Success", employees)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO(VarList.Internal_Server_Error, e.getMessage(), null));
        }
    }

    @GetMapping("/employees/search")
    public ResponseEntity<ResponseDTO> searchEmployees(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String department) {
        try {
            List<EmployeeDTO> employees;

            if (name != null && department != null) {
                employees = employeeService.searchEmployeesByNameAndDepartment(name, department);
            } else if (name != null) {
                employees = employeeService.searchEmployeesByName(name);
            } else if (department != null) {
                employees = employeeService.searchEmployeesByDepartment(department);
            } else {
                employees = employeeService.getAllEmployees();
            }

            return ResponseEntity.ok(
                    new ResponseDTO(VarList.OK, "Success", employees)
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO(VarList.Internal_Server_Error, e.getMessage(), null));
        }
    }

    // Leave Request Management

    @GetMapping("/leave-requests")
    public ResponseEntity<ResponseDTO> getAllLeaveRequests() {
        try {
            List<LeaveRequestDTO> leaveRequests = leaveRequestService.getAllLeaveRequests();
            return ResponseEntity.ok(
                    new ResponseDTO(VarList.OK, "Success", leaveRequests)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO(VarList.Internal_Server_Error, e.getMessage(), null));
        }
    }

    @GetMapping("/leave-requests/status/{status}")
    public ResponseEntity<ResponseDTO> getLeaveRequestsByStatus(@PathVariable String status) {
        try {
            List<LeaveRequestDTO> leaveRequests = leaveRequestService.getLeaveRequestsByStatus(status);
            return ResponseEntity.ok(
                    new ResponseDTO(VarList.OK, "Success", leaveRequests)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO(VarList.Internal_Server_Error, e.getMessage(), null));
        }
    }

    @PatchMapping("/leave-requests/{leaveId}/status")
    public ResponseEntity<ResponseDTO> updateLeaveStatus(
            @PathVariable int leaveId,
            @RequestParam String status) {
        try {
            // Validate status
            if (!status.equals("APPROVED") && !status.equals("REJECTED")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ResponseDTO(VarList.Bad_Request, "Status must be APPROVED or REJECTED", null));
            }

            int res = leaveRequestService.updateLeaveStatus(leaveId, status);

            if (res == VarList.OK) {
                return ResponseEntity.ok(
                        new ResponseDTO(VarList.OK, "Leave request " + status.toLowerCase() + " successfully", null)
                );
            }

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO(VarList.Not_Found, "Leave request not found", null));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO(VarList.Internal_Server_Error, e.getMessage(), null));
        }
    }
}