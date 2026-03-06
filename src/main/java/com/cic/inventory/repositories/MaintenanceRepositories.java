package com.cic.inventory.repositories;

import com.cic.inventory.entities.Maintenance;
import com.cic.inventory.entities.MaintenanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaintenanceRepositories extends JpaRepository<Maintenance,Long> {
    List<Maintenance> findByAssetId(String assetId);
    List<Maintenance> findByStatus(MaintenanceStatus status);
    boolean existsByTicketNo(String ticketNo);
}
