package com.cic.inventory.services.impl;

import com.cic.inventory.entities.Employee;
import com.cic.inventory.exceptions.InventoryException;
import com.cic.inventory.repositories.EmployeeRepositories;
import com.cic.inventory.services.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    private final ModelMapper modelMapper;
    private final EmployeeRepositories employeeRepositories;
    @Override
    public Employee createNewEmployee(Employee employee) {
        try {
            return employeeRepositories.save(employee);
        }catch (Exception exception) {
            log.error("Failed to create new employee", exception);
            throw new InventoryException("Failed to create new employee", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Page<Employee> getAllEmployees(Pageable pageable) {
        try {
            return employeeRepositories.findAll(pageable);
        } catch (Exception exception) {
            log.error("Failed to get all employees", exception);
            throw new InventoryException("Failed to get all employees", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Employee getEmployeeById(Long id) {
        try {
            return employeeRepositories.findById(id).orElseThrow(
                    () -> new InventoryException("Employee Not Found", HttpStatus.NOT_FOUND)
            );
        }catch (InventoryException inventoryException){
            log.warn("Employee not found with id: {} to fetch", id, inventoryException);
            throw new InventoryException("Employee Not found", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public Employee updateEmployeeById(Long id, Employee updatedEmployee) {
        try {
            Employee employee =  employeeRepositories.findById(id).orElseThrow(
                    () -> new InventoryException("Location Not Found", HttpStatus.NOT_FOUND)
            );
            modelMapper.map(updatedEmployee, employee);
            return employeeRepositories.save(employee);
        }catch (InventoryException inventoryException){
            log.warn("Employee not found with id: {} to fetch", id, inventoryException);
            throw new InventoryException("Employee Not found", HttpStatus.NOT_FOUND);

        } catch (Exception exception) {
            log.error("Error updating employee", exception);
            throw new InventoryException("Failed to update employee", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void deleteEmployee(Long id) {
        try {
            employeeRepositories.deleteById(id);
        } catch (Exception exception) {
            log.error("Failed to delete employee with id {}", id, exception);
            throw new InventoryException("Failed to delete employee", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
