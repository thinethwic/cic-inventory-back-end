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
    private final DepartmentRepositories departmentRepositories;
    private final LocationRepositories locationRepositories;

    // ── helper: resolve a managed Location from just an ID ref ──────────────
    private Location resolveLocation(Long locationId) {
        if (locationId == null) {
            throw new InventoryException("Location is required", HttpStatus.BAD_REQUEST);
        }
        return locationRepositories.findById(locationId)
                .orElseThrow(() -> new InventoryException(
                        "Location not found: " + locationId, HttpStatus.NOT_FOUND));
    }

    @Override
    public Department createNewDepartment(Department department) {
        try {
            Long locationId = (department.getLocation() != null)
                    ? department.getLocation().getId()
                    : null;

            Location location = resolveLocation(locationId);
            department.setLocation(location);  // replace transient ref with managed entity

            return departmentRepositories.save(department);

        } catch (InventoryException e) {
            throw e;  // let custom exceptions propagate as-is
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation while creating Department: {}", e.getMessage());
            throw new InventoryException("Department with this code already exists", HttpStatus.CONFLICT);
        } catch (Exception e) {
            log.error("Failed to create new department", e);
            throw new InventoryException("Failed to create new department", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Page<Department> getAllDepartments(Pageable pageable) {
        try {
            return departmentRepositories.findAll(pageable);
        } catch (Exception e) {
            log.error("Failed to get all departments", e);
            throw new InventoryException("Failed to get all departments", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Department getDepartmentById(Long id) {
        return departmentRepositories.findById(id)
                .orElseThrow(() -> new InventoryException("Department not found", HttpStatus.NOT_FOUND));
    }

    @Override
    public Department updateDepartmentById(Long id, Department updatedDepartment) {
        try {
            Department department = departmentRepositories.findById(id)
                    .orElseThrow(() -> new InventoryException("Department not found", HttpStatus.NOT_FOUND));

            // ── manually map only the safe scalar fields ─────────────────────
            // Do NOT use modelMapper here — it overwrites location with a
            // transient entity ref, which causes a TransientPropertyValueException
            if (updatedDepartment.getName() != null) {
                department.setName(updatedDepartment.getName());
            }
            if (updatedDepartment.getCode() != null) {
                department.setCode(updatedDepartment.getCode());
            }

            // ── resolve location from DB by ID ────────────────────────────────
            Long locationId = (updatedDepartment.getLocation() != null)
                    ? updatedDepartment.getLocation().getId()
                    : null;

            if (locationId != null) {
                Location location = resolveLocation(locationId);
                department.setLocation(location);
            }

            return departmentRepositories.save(department);

        } catch (InventoryException e) {
            throw e;
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation while updating Department: {}", e.getMessage());
            throw new InventoryException("Department with this code already exists", HttpStatus.CONFLICT);
        } catch (Exception e) {
            log.error("Error updating department with id {}", id, e);
            throw new InventoryException("Failed to update department", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void deleteDepartment(Long id) {
        try {
            departmentRepositories.deleteById(id);
        } catch (Exception e) {
            log.error("Failed to delete department with id {}", id, e);
            throw new InventoryException("Failed to delete department", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
