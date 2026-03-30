package com.cic.inventory.services.impl;

import com.cic.inventory.entities.Department;
import com.cic.inventory.entities.Employee;
import com.cic.inventory.entities.Location;
import com.cic.inventory.exceptions.InventoryException;
import com.cic.inventory.repositories.DepartmentRepositories;
import com.cic.inventory.repositories.EmployeeRepositories;
import com.cic.inventory.repositories.LocationRepositories;
import com.cic.inventory.services.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    private final ModelMapper modelMapper;
    private final EmployeeRepositories employeeRepositories;
    private final DepartmentRepositories departmentRepositories;
    private final LocationRepositories locationRepositories;

    private String generateNextEmpId() {
        Optional<Employee> lastEmployee = employeeRepositories.findTopByOrderByIdDesc();

        if (lastEmployee.isEmpty() || lastEmployee.get().getEmpId() == null) {
            return "EMP001";
        }

        String lastEmpId = lastEmployee.get().getEmpId(); // example EMP007
        String numericPart = lastEmpId.replaceAll("\\D+", ""); // 007

        int nextNumber = Integer.parseInt(numericPart) + 1;

        return String.format("EMP%03d", nextNumber);
    }

    @Override
    public Employee createNewEmployee(Employee employee) {
        try {
            if (employee.getDepartment() == null || employee.getDepartment().getId() == null) {
                throw new InventoryException("Department is required", HttpStatus.BAD_REQUEST);
            }

            if (employee.getLocation() == null || employee.getLocation().getId() == null) {
                throw new InventoryException("Location is required", HttpStatus.BAD_REQUEST);
            }

            Department department = departmentRepositories.findById(employee.getDepartment().getId())
                    .orElseThrow(() -> new InventoryException("Department not found", HttpStatus.NOT_FOUND));

            Location location = locationRepositories.findById(employee.getLocation().getId())
                    .orElseThrow(() -> new InventoryException("Location not found", HttpStatus.NOT_FOUND));

            employee.setDepartment(department);
            employee.setLocation(location);
            employee.setEmpId(generateNextEmpId());

            return employeeRepositories.save(employee);

        } catch (InventoryException inventoryException) {
            log.error("Failed to create new employee: {}", inventoryException.getMessage());
            throw inventoryException;
        } catch (Exception exception) {
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
            Employee employee = employeeRepositories.findById(id).orElseThrow(
                    () -> new InventoryException("Employee Not Found", HttpStatus.NOT_FOUND)
            );

            // Handle department update
            if (updatedEmployee.getDepartment() != null && updatedEmployee.getDepartment().getId() != null) {
                Department department = departmentRepositories.findById(updatedEmployee.getDepartment().getId())
                        .orElseThrow(() -> new InventoryException("Department not found", HttpStatus.NOT_FOUND));
                employee.setDepartment(department);
            }

            // Handle location update
            if (updatedEmployee.getLocation() != null && updatedEmployee.getLocation().getId() != null) {
                Location location = locationRepositories.findById(updatedEmployee.getLocation().getId())
                        .orElseThrow(() -> new InventoryException("Location not found", HttpStatus.NOT_FOUND));
                employee.setLocation(location);
            }

            modelMapper.map(updatedEmployee, employee);
            employee.setId(id); // restore ID after mapping

            return employeeRepositories.save(employee);

        } catch (InventoryException inventoryException) {
            log.warn("InventoryException while updating employee with id: {}", id, inventoryException);
            throw inventoryException; // preserve original status and message

        } catch (Exception exception) {
            log.error("Error updating employee with id: {}", id, exception);
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
