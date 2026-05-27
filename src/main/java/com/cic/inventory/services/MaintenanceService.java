package com.cic.inventory.services;

import com.cic.inventory.dtos.MaintenanceDTO;
import com.cic.inventory.dtos.MaintenanceResponse;
import com.cic.inventory.dtos.responses.AssetResponseDTO;
import com.cic.inventory.entities.Maintenance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MaintenanceService {
    // MaintenanceService interface
    Maintenance createNewMaintenance(MaintenanceDTO dto);
    Maintenance updateMaintenanceById(Long id, MaintenanceDTO dto);
    Maintenance getMaintenanceById(Long id);
    Page<Maintenance> getAllMaintenance(Pageable pageable);
    Page<Maintenance> getAllMaintenanceFiltered(Pageable pageable, String search, String status, String priority, String location);
    void deleteMaintenance(Long id);
    Page<Maintenance> getAssetsByLocation(String locationName, Pageable pageable);
}