package com.lk.smartleave.SmartLeaveBackend.service.impl;

import com.lk.smartleave.SmartLeaveBackend.dto.EmployeeDTO;
import com.lk.smartleave.SmartLeaveBackend.entity.Employee;
import com.lk.smartleave.SmartLeaveBackend.entity.EmployeeStatus;
import com.lk.smartleave.SmartLeaveBackend.entity.User;
import com.lk.smartleave.SmartLeaveBackend.repo.EmployeeRepository;
import com.lk.smartleave.SmartLeaveBackend.repo.UserRepository;
import com.lk.smartleave.SmartLeaveBackend.service.EmployeeService;
import com.lk.smartleave.SmartLeaveBackend.util.VarList;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public int saveEmployee(EmployeeDTO employeeDTO) {
        if (employeeRepository.existsByEmail(employeeDTO.getEmail())) {
            return VarList.Not_Acceptable; // Email already exists
        }

        // Check if user exists for the given email
        User user = userRepository.findByEmail(employeeDTO.getUserEmail());
        if (user == null) {
            return VarList.Not_Found; // User not found
        }

        // Get managed user reference from database to avoid detached entity
        User managedUser = userRepository.findById(user.getUid())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + user.getUid()));

        // Create employee manually instead of using modelMapper to avoid detached entity issues
        Employee employee = new Employee();
        employee.setName(employeeDTO.getName());
        employee.setEmail(employeeDTO.getEmail());
        employee.setDepartment(employeeDTO.getDepartment());
        employee.setJoiningDate(LocalDate.now());
        employee.setStatus(EmployeeStatus.ACTIVE.name());
        employee.setUser(managedUser); // Use the managed entity

        employeeRepository.save(employee);
        return VarList.Created;
    }

    @Override
    public int updateEmployee(EmployeeDTO employeeDTO) {
        Optional<Employee> optionalEmployee = employeeRepository.findById(employeeDTO.getId());
        if (optionalEmployee.isPresent()) {
            Employee existingEmployee = optionalEmployee.get();

            // Update fields
            existingEmployee.setName(employeeDTO.getName());
            existingEmployee.setDepartment(employeeDTO.getDepartment());

            // Handle status conversion safely
            if (employeeDTO.getStatus() != null) {
                try {
                    existingEmployee.setStatus(EmployeeStatus.valueOf(employeeDTO.getStatus()).name());
                } catch (IllegalArgumentException e) {
                    // If invalid status, keep current status
                    System.err.println("Invalid status provided: " + employeeDTO.getStatus());
                }
            }

            employeeRepository.save(existingEmployee);
            return VarList.OK;
        }
        return VarList.Not_Found;
    }

    @Override
    public int deleteEmployee(int id) {
        if (employeeRepository.existsById(id)) {
            employeeRepository.deleteById(id);
            return VarList.OK;
        }
        return VarList.Not_Found;
    }

    @Override
    public EmployeeDTO getEmployeeById(int id) {
        Optional<Employee> employee = employeeRepository.findById(id);
        if (employee.isPresent()) {
            Employee emp = employee.get();
            EmployeeDTO dto = modelMapper.map(emp, EmployeeDTO.class);
            // Set userEmail for response
            if (emp.getUser() != null) {
                dto.setUserEmail(emp.getUser().getEmail());
            }
            return dto;
        }
        return null;
    }

    @Override
    public EmployeeDTO getEmployeeByEmail(String email) {
        Optional<Employee> employee = employeeRepository.findByEmail(email);
        if (employee.isPresent()) {
            Employee emp = employee.get();
            EmployeeDTO dto = modelMapper.map(emp, EmployeeDTO.class);
            // Set userEmail for response
            if (emp.getUser() != null) {
                dto.setUserEmail(emp.getUser().getEmail());
            }
            return dto;
        }
        return null;
    }

    @Override
    public List<EmployeeDTO> getAllEmployees() {
        List<Employee> employees = employeeRepository.findAll();
        List<EmployeeDTO> employeeDTOs = modelMapper.map(employees, new TypeToken<List<EmployeeDTO>>(){}.getType());

        // Set userEmail for each employee
        for (int i = 0; i < employees.size(); i++) {
            if (employees.get(i).getUser() != null) {
                employeeDTOs.get(i).setUserEmail(employees.get(i).getUser().getEmail());
            }
        }

        return employeeDTOs;
    }

    @Override
    public List<EmployeeDTO> searchEmployeesByName(String name) {
        List<Employee> employees = employeeRepository.findByNameContaining(name);
        List<EmployeeDTO> employeeDTOs = modelMapper.map(employees, new TypeToken<List<EmployeeDTO>>(){}.getType());

        // Set userEmail for each employee
        for (int i = 0; i < employees.size(); i++) {
            if (employees.get(i).getUser() != null) {
                employeeDTOs.get(i).setUserEmail(employees.get(i).getUser().getEmail());
            }
        }

        return employeeDTOs;
    }

    @Override
    public List<EmployeeDTO> searchEmployeesByDepartment(String department) {
        List<Employee> employees = employeeRepository.findByDepartment(department);
        List<EmployeeDTO> employeeDTOs = modelMapper.map(employees, new TypeToken<List<EmployeeDTO>>(){}.getType());

        // Set userEmail for each employee
        for (int i = 0; i < employees.size(); i++) {
            if (employees.get(i).getUser() != null) {
                employeeDTOs.get(i).setUserEmail(employees.get(i).getUser().getEmail());
            }
        }

        return employeeDTOs;
    }

    @Override
    public List<EmployeeDTO> searchEmployeesByNameAndDepartment(String name, String department) {
        List<Employee> employees = employeeRepository.findByNameAndDepartment(name, department);
        List<EmployeeDTO> employeeDTOs = modelMapper.map(employees, new TypeToken<List<EmployeeDTO>>(){}.getType());

        // Set userEmail for each employee
        for (int i = 0; i < employees.size(); i++) {
            if (employees.get(i).getUser() != null) {
                employeeDTOs.get(i).setUserEmail(employees.get(i).getUser().getEmail());
            }
        }

        return employeeDTOs;
    }
}