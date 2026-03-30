package com.cic.inventory.services;

import com.cic.inventory.dtos.MaintenanceDTO;
import com.cic.inventory.dtos.responses.AssetResponseDTO;
import com.cic.inventory.entities.Maintenance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MaintenanceService {
    Maintenance createNewMaintenance(MaintenanceDTO maintenanceDTO);
    Page<Maintenance> getAllMaintenance(Pageable pageable);
    Maintenance getMaintenanceById(Long id);
    Maintenance updateMaintenanceById(Long id, MaintenanceDTO maintenanceDTO);
    void deleteMaintenance(Long id);

    Page<Maintenance> getAssetsByLocation(String locationName, Pageable pageable);
    Page<Maintenance> getAllMaintenanceFiltered(
            Pageable pageable,
            String search,
            String status,
            String priority,
            String location
    );
}