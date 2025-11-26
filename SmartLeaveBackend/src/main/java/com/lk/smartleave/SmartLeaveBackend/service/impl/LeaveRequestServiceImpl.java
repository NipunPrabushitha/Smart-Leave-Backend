package com.lk.smartleave.SmartLeaveBackend.service.impl;

import com.lk.smartleave.SmartLeaveBackend.dto.LeaveRequestDTO;
import com.lk.smartleave.SmartLeaveBackend.entity.Employee;
import com.lk.smartleave.SmartLeaveBackend.entity.LeaveRequest;
import com.lk.smartleave.SmartLeaveBackend.entity.LeaveStatus;
import com.lk.smartleave.SmartLeaveBackend.entity.LeaveType;
import com.lk.smartleave.SmartLeaveBackend.repo.EmployeeRepository;
import com.lk.smartleave.SmartLeaveBackend.repo.LeaveRequestRepository;
import com.lk.smartleave.SmartLeaveBackend.service.LeaveRequestService;
import com.lk.smartleave.SmartLeaveBackend.util.VarList;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class LeaveRequestServiceImpl implements LeaveRequestService {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public int applyForLeave(LeaveRequestDTO leaveRequestDTO) {
        Optional<Employee> employee = employeeRepository.findById(leaveRequestDTO.getEmployeeId());
        if (employee.isEmpty()) {
            return VarList.Not_Found; // Employee not found
        }

        // Check if employee is ACTIVE
        Employee emp = employee.get();
        if (!"ACTIVE".equals(emp.getStatus())) {
            return VarList.Not_Acceptable; // Employee is not active
        }

        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setEmployee(emp);
        leaveRequest.setLeaveType(LeaveType.valueOf(leaveRequestDTO.getLeaveType()));
        leaveRequest.setStartDate(leaveRequestDTO.getStartDate());
        leaveRequest.setEndDate(leaveRequestDTO.getEndDate());
        leaveRequest.setReason(leaveRequestDTO.getReason());
        leaveRequest.setStatus(LeaveStatus.PENDING);
        leaveRequest.setAppliedDate(LocalDate.now());

        leaveRequestRepository.save(leaveRequest);
        return VarList.Created;
    }

    @Override
    public int updateLeaveStatus(int leaveId, String status) {
        Optional<LeaveRequest> leaveRequest = leaveRequestRepository.findById(leaveId);
        if (leaveRequest.isPresent()) {
            LeaveRequest request = leaveRequest.get();
            request.setStatus(LeaveStatus.valueOf(status));
            leaveRequestRepository.save(request);
            return VarList.OK;
        }
        return VarList.Not_Found;
    }

    @Override
    public List<LeaveRequestDTO> getAllLeaveRequests() {
        List<LeaveRequest> leaveRequests = leaveRequestRepository.findAllOrderByAppliedDateDesc();
        List<LeaveRequestDTO> dtos = modelMapper.map(leaveRequests, new TypeToken<List<LeaveRequestDTO>>(){}.getType());

        // Add employee details to DTO
        for (int i = 0; i < leaveRequests.size(); i++) {
            dtos.get(i).setEmployeeName(leaveRequests.get(i).getEmployee().getName());
            dtos.get(i).setEmployeeEmail(leaveRequests.get(i).getEmployee().getEmail());
        }

        return dtos;
    }

    @Override
    public List<LeaveRequestDTO> getLeaveRequestsByEmployee(int employeeId) {
        List<LeaveRequest> leaveRequests = leaveRequestRepository.findByEmployeeId(employeeId);
        return modelMapper.map(leaveRequests, new TypeToken<List<LeaveRequestDTO>>(){}.getType());
    }

    @Override
    public List<LeaveRequestDTO> getLeaveRequestsByEmployeeEmail(String email) {
        List<LeaveRequest> leaveRequests = leaveRequestRepository.findByEmployeeEmail(email);
        return modelMapper.map(leaveRequests, new TypeToken<List<LeaveRequestDTO>>(){}.getType());
    }

    @Override
    public List<LeaveRequestDTO> getLeaveRequestsByStatus(String status) {
        List<LeaveRequest> leaveRequests = leaveRequestRepository.findByStatus(LeaveStatus.valueOf(status));
        List<LeaveRequestDTO> dtos = modelMapper.map(leaveRequests, new TypeToken<List<LeaveRequestDTO>>(){}.getType());

        // Add employee details to DTO
        for (int i = 0; i < leaveRequests.size(); i++) {
            dtos.get(i).setEmployeeName(leaveRequests.get(i).getEmployee().getName());
            dtos.get(i).setEmployeeEmail(leaveRequests.get(i).getEmployee().getEmail());
        }

        return dtos;
    }

    @Override
    public LeaveRequestDTO getLeaveRequestById(int id) {
        Optional<LeaveRequest> leaveRequest = leaveRequestRepository.findById(id);
        if (leaveRequest.isPresent()) {
            LeaveRequestDTO dto = modelMapper.map(leaveRequest.get(), LeaveRequestDTO.class);
            dto.setEmployeeName(leaveRequest.get().getEmployee().getName());
            dto.setEmployeeEmail(leaveRequest.get().getEmployee().getEmail());
            return dto;
        }
        return null;
    }
}