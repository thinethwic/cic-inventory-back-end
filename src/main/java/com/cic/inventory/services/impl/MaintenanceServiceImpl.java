package com.cic.inventory.services.impl;

import com.cic.inventory.dtos.MaintenanceDTO;
import com.cic.inventory.entities.Asset;
import com.cic.inventory.entities.Maintenance;
import com.cic.inventory.entities.Supplier;
import com.cic.inventory.exceptions.InventoryException;
import com.cic.inventory.repositories.AssetRepositories;
import com.cic.inventory.repositories.MaintenanceRepositories;
import com.cic.inventory.repositories.SupplierRepositories;
import com.cic.inventory.services.MaintenanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MaintenanceServiceImpl implements MaintenanceService {

    private final MaintenanceRepositories maintenanceRepositories;
    private final AssetRepositories assetRepositories;
    private final SupplierRepositories supplierRepositories;

    private String generateNextTicketNo() {
        Optional<Maintenance> lastMaintenance = maintenanceRepositories.findTopByOrderByIdDesc();

        if (lastMaintenance.isEmpty() || lastMaintenance.get().getTicketNo() == null) {
            return "MT-0001";
        }

        String lastTicketNo = lastMaintenance.get().getTicketNo(); // example EMP007
        String numericPart = lastTicketNo.replaceAll("\\D+", ""); // 007

        int nextNumber = Integer.parseInt(numericPart) + 1;

        return String.format("EMP%03d", nextNumber);
    }

    @Override
    public Maintenance createNewMaintenance(MaintenanceDTO dto) {
        try {
            Asset asset = assetRepositories.findById(dto.getAssetId())
                    .orElseThrow(() -> new InventoryException("Asset not found", HttpStatus.NOT_FOUND));

            Supplier supplier = supplierRepositories.findById(dto.getSupplierId())
                    .orElseThrow(() -> new InventoryException("Supplier not found", HttpStatus.NOT_FOUND));

            Maintenance maintenance = Maintenance.builder()
                    .ticketNo(dto.getTicketNo())
                    .asset(asset)
                    .supplier(supplier)
                    .issueTitle(dto.getIssueTitle())
                    .description(dto.getDescription())
                    .priority(dto.getPriority())
                    .status(dto.getStatus())
                    .reportedDate(dto.getReportedDate())
                    .dueDate(dto.getDueDate())
                    .assignedTo(dto.getAssignedTo())
                    .cost(dto.getCost())
                    .notes(dto.getNotes())
                    .build();
            maintenance.setTicketNo(generateNextTicketNo());

            return maintenanceRepositories.save(maintenance);

        }catch (DataIntegrityViolationException dataIntegrityViolationException){
            throw new InventoryException("Duplicate Key Constrain from Forging Key",HttpStatus.CONFLICT);
        } catch (InventoryException inventoryException) {
            throw new InventoryException("Not found Exception", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Failed to create maintenance", e);
            throw new InventoryException("Failed to create Maintenance", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Page<Maintenance> getAllMaintenance(Pageable pageable) {
        try {
            return maintenanceRepositories.findAll(pageable);
        } catch (Exception e) {
            log.error("Failed to fetch all maintenance", e);
            throw new InventoryException("Failed to fetch all Maintenance", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Maintenance getMaintenanceById(Long id) {
        return maintenanceRepositories.findById(id)
                .orElseThrow(() -> {
                    log.warn("Maintenance not found with id: {}", id);
                    return new InventoryException("Maintenance not found", HttpStatus.NOT_FOUND);
                });
    }

    @Override
    public Maintenance updateMaintenanceById(Long id, MaintenanceDTO dto) {
        try {
            Maintenance maintenance = maintenanceRepositories.findById(id)
                    .orElseThrow(() -> new InventoryException("Maintenance not found", HttpStatus.NOT_FOUND));

            Asset asset = assetRepositories.findById(dto.getAssetId())
                    .orElseThrow(() -> new InventoryException("Asset not found", HttpStatus.NOT_FOUND));

            Supplier supplier = supplierRepositories.findById(dto.getSupplierId())
                    .orElseThrow(() -> new InventoryException("Supplier not found", HttpStatus.NOT_FOUND));

            maintenance.setTicketNo(dto.getTicketNo());
            maintenance.setAsset(asset);
            maintenance.setSupplier(supplier);
            maintenance.setIssueTitle(dto.getIssueTitle());
            maintenance.setDescription(dto.getDescription());
            maintenance.setPriority(dto.getPriority());
            maintenance.setStatus(dto.getStatus());
            maintenance.setReportedDate(dto.getReportedDate());
            maintenance.setDueDate(dto.getDueDate());
            maintenance.setAssignedTo(dto.getAssignedTo());
            maintenance.setCost(dto.getCost());
            maintenance.setNotes(dto.getNotes());

            return maintenanceRepositories.save(maintenance);

        } catch (InventoryException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to update maintenance with id {}", id, e);
            throw new InventoryException("Failed to update Maintenance", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void deleteMaintenance(Long id) {
        if (!maintenanceRepositories.existsById(id)) {
            throw new InventoryException("Maintenance not found", HttpStatus.NOT_FOUND);
        }
        try {
            maintenanceRepositories.deleteById(id);
        } catch (Exception e) {
            log.error("Failed to delete maintenance with id {}", id, e);
            throw new InventoryException("Failed to delete maintenance", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}