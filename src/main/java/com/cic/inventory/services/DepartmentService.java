package com.cic.inventory.services;

import com.cic.inventory.entities.Asset;
import com.cic.inventory.entities.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DepartmentService {
    Department createNewDepartment(Department  department);
    Page<Department> getAllDepartments(Pageable pageable);
    Department getDepartmentById(Long id);
    Department updateDepartmentById(Long id, Department updatedDepartment);
    void deleteDepartment(Long id);
}
