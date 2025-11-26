package com.lk.smartleave.SmartLeaveBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO {
    private Integer id;
    private String name;
    private String email;
    private String department;
    private LocalDate joiningDate;
    private String status; // ACTIVE / INACTIVE
    private String userEmail; // For creating employee with existing user
}