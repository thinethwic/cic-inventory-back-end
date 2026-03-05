package com.cic.inventory.services;


import com.cic.inventory.entities.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmployeeService {
    Employee createNewEmployee(Employee  employee);
    Page<Employee> getAllEmployees(Pageable pageable);
    Employee getEmployeeById(Long id);
    Employee updateEmployeeById(Long id, Employee updatedEmployee);
    void deleteEmployee(Long id);
}
