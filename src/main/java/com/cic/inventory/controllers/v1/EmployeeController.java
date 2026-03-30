package com.cic.inventory.controllers.v1;

import com.cic.inventory.controllers.AbstractController;
import com.cic.inventory.dtos.EmployeeDTO;
import com.cic.inventory.entities.Employee;
import com.cic.inventory.services.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController extends AbstractController {

    private final ModelMapper modelMapper;
    private final EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<Page<EmployeeDTO>> getAllEmployees(Pageable pageable) {
        Page<EmployeeDTO> employees = employeeService.getAllEmployees(pageable)
                .map(emp -> modelMapper.map(emp, EmployeeDTO.class));
        return sendOkResponse(employees);
    }

    @GetMapping("{id}")
    public ResponseEntity<EmployeeDTO> getEmployeeById(@PathVariable Long id) {
        Employee employee = employeeService.getEmployeeById(id);
        return sendOkResponse(modelMapper.map(employee, EmployeeDTO.class));
    }

    @PostMapping
    public ResponseEntity<EmployeeDTO> createEmployee(@Validated @RequestBody EmployeeDTO employeeDTO) {
        Employee employee = modelMapper.map(employeeDTO, Employee.class);
        Employee createdEmployee = employeeService.createNewEmployee(employee);
        return sendCreatedResponse(modelMapper.map(createdEmployee, EmployeeDTO.class));
    }

    @PutMapping("{id}")
    public ResponseEntity<EmployeeDTO> updateEmployee(@PathVariable Long id, @Validated @RequestBody EmployeeDTO updateEmployeeDTO) {
        Employee employee = modelMapper.map(updateEmployeeDTO, Employee.class);
        Employee updatedEmployee = employeeService.updateEmployeeById(id, employee);
        return sendOkResponse(modelMapper.map(updatedEmployee, EmployeeDTO.class));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return sendNoContentResponse();
    }
}