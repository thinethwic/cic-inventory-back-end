package com.cic.inventory.repositories;

import com.cic.inventory.entities.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepositories extends JpaRepository<Employee, Long> {
}
