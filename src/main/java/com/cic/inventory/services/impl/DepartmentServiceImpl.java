package com.cic.inventory.services.impl;
import com.cic.inventory.entities.Department;
import com.cic.inventory.entities.Location;
import com.cic.inventory.exceptions.InventoryException;
import com.cic.inventory.repositories.DepartmentRepositories;
import com.cic.inventory.repositories.LocationRepositories;
import com.cic.inventory.services.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {
    private final ModelMapper modelMapper;
    private final DepartmentRepositories departmentRepositories;
    private final LocationRepositories locationRepositories;

    @Override
    public Department createNewDepartment(Department  department) {
        try {
            if (department.getLocation() == null || department.getLocation().getId() == null) {
                throw new InventoryException("Location is required", HttpStatus.BAD_REQUEST);
            }

            Location location = locationRepositories.findById(department.getLocation().getId())
                    .orElseThrow(() -> new InventoryException("Location not found", HttpStatus.NOT_FOUND));

            department.setLocation(location);
            return departmentRepositories.save(department);
        } catch (DataIntegrityViolationException e){
            log.error("Data integrity violation while creating Department: {}", e.getMessage());
            throw new InventoryException("Department with this code already exists", HttpStatus.CONFLICT);
        } catch (Exception exception) {
            log.error("Failed to create new department", exception);
            throw new InventoryException("Failed to create new department", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Page<Department> getAllDepartments(Pageable pageable) {
        try {
            return departmentRepositories.findAll(pageable);
        } catch (Exception exception) {
            log.error("Failed to get all departments", exception);
            throw new InventoryException("Failed to get all departments", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Department getDepartmentById(Long id) {
        try {
            Department department =  departmentRepositories.findById(id).orElseThrow(
                    () -> new InventoryException("Department Not Found", HttpStatus.NOT_FOUND)
            );
            log.info("Successfully fetched asset {}", id);
            return department;
        }catch (InventoryException inventoryException){
            log.warn("Department not found with id: {} to fetch", id, inventoryException);
            throw new InventoryException("Department Not found", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public Department updateDepartmentById(Long id, Department updatedDepartment) {
        try {
            Department department =  departmentRepositories.findById(id).orElseThrow(
                    () -> new InventoryException("Department Not Found", HttpStatus.NOT_FOUND)
            );
            modelMapper.map(updatedDepartment, department);
            return departmentRepositories.save(department);
        }catch (InventoryException inventoryException){
            log.warn("Department not found with id: {} to fetch", id, inventoryException);
            throw new InventoryException("Department Not found", HttpStatus.NOT_FOUND);

        } catch (Exception exception) {
            log.error("Error updating department", exception);
            throw new InventoryException("Failed to update department", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void deleteDepartment(Long id) {
        try {
            departmentRepositories.deleteById(id);
        } catch (Exception exception) {
            log.error("Failed to delete department with id {}", id, exception);
            throw new InventoryException("Failed to delete department", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
