package com.cic.inventory.services.impl;

import com.cic.inventory.dtos.MaintenanceDTO;
import com.cic.inventory.entities.Asset;
import com.cic.inventory.entities.Maintenance;
import com.cic.inventory.entities.types.MaintenanceStatus;
import com.cic.inventory.exceptions.InventoryException;
import com.cic.inventory.repositories.AssetRepositories;
import com.cic.inventory.repositories.MaintenanceRepositories;
import com.cic.inventory.services.MaintenanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MaintenanceServiceImpl implements MaintenanceService {

    private final MaintenanceRepositories maintenanceRepositories;
    private final AssetRepositories assetRepositories;

    // Closed statuses — a new ticket is allowed after one of these
    private static final List<MaintenanceStatus> CLOSED_STATUSES = List.of(
            MaintenanceStatus.COMPLETED,
            MaintenanceStatus.CANCELLED,
            MaintenanceStatus.CANNOT_REPAIR
    );

    // ── Ticket number generator ───────────────────────────────────────────────
    private synchronized String generateNextTicketNo() {
        Optional<Maintenance> last =
                maintenanceRepositories.findTopByOrderByIdDesc();

        if (last.isEmpty() || last.get().getTicketNo() == null) {
            return "MT-0001";
        }

        String lastTicketNo = last.get().getTicketNo();
        String numericPart  = lastTicketNo.replaceAll("\\D+", "");

        try {
            int next = Integer.parseInt(numericPart) + 1;
            return String.format("MT-%04d", next);
        } catch (NumberFormatException e) {
            return "MT-" + System.currentTimeMillis();
        }
    }

    // ── CREATE ────────────────────────────────────────────────────────────────
    @Override
    public Maintenance createNewMaintenance(MaintenanceDTO dto) {
        try {
            Asset asset = assetRepositories.findById(dto.getAssetId())
                    .orElseThrow(() -> new InventoryException(
                            "Asset not found", HttpStatus.NOT_FOUND));

            // One active ticket per asset at a time
            boolean hasActiveTicket =
                    maintenanceRepositories.existsByAsset_IdAndStatusNotIn(
                            dto.getAssetId(), CLOSED_STATUSES
                    );

            if (hasActiveTicket) {
                throw new InventoryException(
                        "This asset already has an active maintenance ticket. " +
                                "Please complete or cancel it before creating a new one.",
                        HttpStatus.CONFLICT
                );
            }

            Maintenance maintenance = Maintenance.builder()
                    .ticketNo(generateNextTicketNo())
                    .asset(asset)
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

            return maintenanceRepositories.save(maintenance);

        } catch (InventoryException e) {
            throw e;
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation creating maintenance", e);
            throw new InventoryException(
                    "Failed to create ticket — data integrity error.",
                    HttpStatus.CONFLICT
            );
        } catch (Exception e) {
            log.error("Failed to create maintenance", e);
            throw new InventoryException(
                    "Failed to create Maintenance",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
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
                    .orElseThrow(() -> new InventoryException(
                            "Maintenance not found", HttpStatus.NOT_FOUND));

            Asset asset = assetRepositories.findById(dto.getAssetId())
                    .orElseThrow(() -> new InventoryException(
                            "Asset not found", HttpStatus.NOT_FOUND));

            // Check for another active ticket on same asset, excluding self
            boolean hasActiveTicket =
                    maintenanceRepositories.existsByAsset_IdAndStatusNotInAndIdNot(
                            dto.getAssetId(), CLOSED_STATUSES, id
                    );

            if (hasActiveTicket) {
                throw new InventoryException(
                        "This asset already has another active maintenance ticket.",
                        HttpStatus.CONFLICT
                );
            }

            // ticketNo is immutable — never overwrite it from the DTO
            maintenance.setAsset(asset);
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
            throw new InventoryException(
                    "Failed to update Maintenance",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
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