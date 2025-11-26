package com.lk.smartleave.SmartLeaveBackend.service;

import com.lk.smartleave.SmartLeaveBackend.dto.EmployeeDTO;

import java.util.List;

public interface EmployeeService {
    int saveEmployee(EmployeeDTO employeeDTO);
    int updateEmployee(EmployeeDTO employeeDTO);
    int deleteEmployee(int id);
    EmployeeDTO getEmployeeById(int id);
    EmployeeDTO getEmployeeByEmail(String email);
    List<EmployeeDTO> getAllEmployees();
    List<EmployeeDTO> searchEmployeesByName(String name);
    List<EmployeeDTO> searchEmployeesByDepartment(String department);
    List<EmployeeDTO> searchEmployeesByNameAndDepartment(String name, String department);
}