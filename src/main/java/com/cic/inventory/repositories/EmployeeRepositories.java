package com.cic.inventory.repositories;

import com.cic.inventory.entities.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeRepositories extends JpaRepository<Employee, Long> {
    Optional<Employee> findTopByOrderByIdDesc();

    boolean existsByEmpId(String empId);
}
