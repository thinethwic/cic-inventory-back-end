package com.cic.inventory.repositories;

import com.cic.inventory.entities.Employee;
import com.cic.inventory.entities.Maintenance;
import com.cic.inventory.entities.types.MaintenanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MaintenanceRepositories extends JpaRepository<Maintenance,Long> {
    Optional<Maintenance> findTopByOrderByIdDesc();

    boolean existsByTicketNo(String ticketNo);
}
