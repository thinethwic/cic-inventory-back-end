package com.cic.inventory.services;

import com.cic.inventory.dtos.MaintenanceRequestDTO;
import com.cic.inventory.dtos.MaintenanceResponseDTO;
import com.cic.inventory.entities.MaintenanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MaintenanceService {

    MaintenanceResponseDTO createMaintenance(MaintenanceRequestDTO requestDTO);

    Page<MaintenanceResponseDTO> getAllMaintenance(Pageable pageable);

    MaintenanceResponseDTO getMaintenanceById(Long id);

    MaintenanceResponseDTO updateMaintenanceById(Long id, MaintenanceRequestDTO requestDTO);

    void deleteMaintenance(Long id);

    List<MaintenanceResponseDTO> getMaintenanceByAssetId(String assetId);

    List<MaintenanceResponseDTO> getMaintenanceByStatus(MaintenanceStatus status);
}
