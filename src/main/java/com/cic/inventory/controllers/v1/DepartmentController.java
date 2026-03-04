package com.cic.inventory.controllers.v1;

import com.cic.inventory.controllers.AbstractController;
import com.cic.inventory.dtos.AssetDTO;
import com.cic.inventory.dtos.DepartmentDTO;
import com.cic.inventory.entities.Asset;
import com.cic.inventory.entities.Department;
import com.cic.inventory.services.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/departments")
@RequiredArgsConstructor
public class DepartmentController extends AbstractController {
    private final ModelMapper modelMapper;
    private final DepartmentService departmentService;

    @GetMapping
    public ResponseEntity<Page<Department>> getAllDepartments(Pageable pageable) {
        Page<Department> departments = departmentService.getAllDepartments(pageable);
        return sendOkResponse(departments);
    }

    @GetMapping("{id}")
    public ResponseEntity<Department> getDepartmentById(@PathVariable Long id) {
        Department department = departmentService.getDepartmentById(id);
        return sendOkResponse(department);
    }

    @PostMapping
    public ResponseEntity<Department> createDepartment(@Validated @RequestBody DepartmentDTO  departmentDTO) {
        Department department = modelMapper.map(departmentDTO,Department.class);
        Department createDepartment = departmentService.createNewDepartment(department);
        return sendCreatedResponse(createDepartment);
    }

    @PutMapping("{id}")
    public ResponseEntity<Department> updateDepartment(@PathVariable Long id, @Valid @RequestBody DepartmentDTO updatedDepDTO) {
        Department department = modelMapper.map(updatedDepDTO, Department.class);
        Department updatedDep = departmentService.updateDepartmentById(id, department);
        return sendOkResponse(updatedDep);

    }

    @DeleteMapping("{id}")
    public ResponseEntity<Department> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return sendNoContentResponse();
    }
}
