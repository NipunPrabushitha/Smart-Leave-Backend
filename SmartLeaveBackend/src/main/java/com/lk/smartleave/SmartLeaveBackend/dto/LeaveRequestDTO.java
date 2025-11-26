package com.lk.smartleave.SmartLeaveBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestDTO {
    private Integer id;
    private Integer employeeId;
    private String leaveType; // CASUAL, SICK, ANNUAL
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private String status; // PENDING, APPROVED, REJECTED
    private LocalDate appliedDate;
    private String employeeName; // For response
    private String employeeEmail; // For response
}