package com.lk.smartleave.SmartLeaveBackend.controller;

import com.lk.smartleave.SmartLeaveBackend.dto.ResponseDTO;
import com.lk.smartleave.SmartLeaveBackend.service.EmployeeService;
import com.lk.smartleave.SmartLeaveBackend.util.VarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
/*@CrossOrigin(origins = {"https://myapp.com", "https://staging.myapp.com"})*/
@RequestMapping("api/v1/employee")
@PreAuthorize("hasAnyAuthority('EMPLOYEE', 'ADMIN')")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    // Employee profile management

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