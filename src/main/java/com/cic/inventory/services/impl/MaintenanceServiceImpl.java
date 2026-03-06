package com.cic.inventory.services.impl;

import com.cic.inventory.dtos.MaintenanceRequestDTO;
import com.cic.inventory.dtos.MaintenanceResponseDTO;
import com.cic.inventory.entities.Maintenance;
import com.cic.inventory.entities.MaintenanceStatus;
import com.cic.inventory.repositories.MaintenanceRepositories;
import com.cic.inventory.services.MaintenanceService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MaintenanceServiceImpl implements MaintenanceService {
    private final MaintenanceRepositories maintenanceRepository;
    private final ModelMapper modelMapper;

    @Override
    public MaintenanceResponseDTO createMaintenance(MaintenanceRequestDTO requestDTO) {
        if (maintenanceRepository.existsByTicketNo(requestDTO.getTicketNo())) {
            throw new RuntimeException("Ticket number already exists: " + requestDTO.getTicketNo());
        }
        Maintenance maintenance = modelMapper.map(requestDTO, Maintenance.class);
        Maintenance saved = maintenanceRepository.save(maintenance);
        return modelMapper.map(saved, MaintenanceResponseDTO.class);
    }

    @Override
    public Page<MaintenanceResponseDTO> getAllMaintenance(Pageable pageable) {
        return maintenanceRepository.findAll(pageable)
                .map(m -> modelMapper.map(m, MaintenanceResponseDTO.class));
    }

    @Override
    public MaintenanceResponseDTO getMaintenanceById(Long id) {
        Maintenance maintenance = maintenanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Maintenance not found with id: " + id));
        return modelMapper.map(maintenance, MaintenanceResponseDTO.class);
    }

    @Override
    public MaintenanceResponseDTO updateMaintenanceById(Long id, MaintenanceRequestDTO requestDTO) {
        Maintenance existing = maintenanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Maintenance not found with id: " + id));

        // ModelMapper skips nulls (setSkipNullEnabled=true in your config)
        // so only fields present in the request will be updated
        modelMapper.map(requestDTO, existing);

        Maintenance updated = maintenanceRepository.save(existing);
        return modelMapper.map(updated, MaintenanceResponseDTO.class);
    }

    @Override
    public void deleteMaintenance(Long id) {
        if (!maintenanceRepository.existsById(id)) {
            throw new RuntimeException("Maintenance not found with id: " + id);
        }
        maintenanceRepository.deleteById(id);
    }

    @Override
    public List<MaintenanceResponseDTO> getMaintenanceByAssetId(String assetId) {
        return maintenanceRepository.findByAssetId(assetId)
                .stream()
                .map(m -> modelMapper.map(m, MaintenanceResponseDTO.class))
                .toList();
    }

    @Override
    public List<MaintenanceResponseDTO> getMaintenanceByStatus(MaintenanceStatus status) {
        return maintenanceRepository.findByStatus(status)
                .stream()
                .map(m -> modelMapper.map(m, MaintenanceResponseDTO.class))
                .toList();
    }
}
