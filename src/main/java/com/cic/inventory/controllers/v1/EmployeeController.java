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
    public ResponseEntity<Page<Employee>> getAllEmployees(Pageable pageable) {
        Page<Employee> employees = employeeService.getAllEmployees(pageable);
        return sendOkResponse(employees);
    }

    @GetMapping("{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
        Employee employee = employeeService.getEmployeeById(id);
        return sendOkResponse(employee);
    }

    @PostMapping
    public ResponseEntity<Employee> createEmployee(@Validated @RequestBody EmployeeDTO employeeDTO) {
        Employee employee = modelMapper.map(employeeDTO,Employee.class);
        Employee createEmployee =employeeService.createNewEmployee(employee);
        return sendCreatedResponse(createEmployee);
    }

    @PutMapping("{id}")
    public ResponseEntity<Employee> updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeDTO updateEmployeeDTO ) {
        Employee employee = modelMapper.map(updateEmployeeDTO, Employee.class);
        Employee updatedEmployeeById = employeeService.updateEmployeeById(id, employee);
        return sendOkResponse(updatedEmployeeById);

    }

    @DeleteMapping("{id}")
    public ResponseEntity<Employee> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return sendNoContentResponse();
    }

}
