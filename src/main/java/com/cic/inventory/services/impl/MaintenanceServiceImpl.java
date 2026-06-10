package com.cic.inventory.services.impl;

import com.cic.inventory.dtos.MaintenanceDTO;
import com.cic.inventory.dtos.MaintenanceResponse;
import com.cic.inventory.dtos.UserSummaryDTO;
import com.cic.inventory.entities.Asset;
import com.cic.inventory.entities.Maintenance;
import com.cic.inventory.entities.User;
import com.cic.inventory.entities.types.MaintenanceStatus;
import com.cic.inventory.exceptions.InventoryException;
import com.cic.inventory.repositories.AssetRepositories;
import com.cic.inventory.repositories.MaintenanceRepositories;
import com.cic.inventory.repositories.UserRepository;
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
    private final UserRepository userRepository;

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
    public Maintenance createNewMaintenance(MaintenanceDTO maintenanceDTO) {
        try {
            Asset asset = assetRepositories.findById(maintenanceDTO.getAssetId())
                    .orElseThrow(() -> new InventoryException(
                            "Asset not found", HttpStatus.NOT_FOUND));

            boolean hasActiveTicket =
                    maintenanceRepositories.existsByAsset_IdAndStatusNotIn(
                            maintenanceDTO.getAssetId(), CLOSED_STATUSES);

            if (hasActiveTicket) {
                throw new InventoryException(
                        "This asset already has an active maintenance ticket. " +
                                "Please complete or cancel it before creating a new one.",
                        HttpStatus.CONFLICT);
            }

            // ── Resolve the creating user ──────────────────────────────────
            User createdBy = resolveUser(maintenanceDTO.getCurrentUserId());

            Maintenance maintenance = Maintenance.builder()
                    .ticketNo(generateNextTicketNo())
                    .asset(asset)
                    .issueTitle(maintenanceDTO.getIssueTitle())
                    .description(maintenanceDTO.getDescription())
                    .priority(maintenanceDTO.getPriority())
                    .status(maintenanceDTO.getStatus())
                    .reportedDate(maintenanceDTO.getReportedDate())
                    .dueDate(maintenanceDTO.getDueDate())
                    .assignedTo(maintenanceDTO.getAssignedTo())
                    .cost(maintenanceDTO.getCost())
                    .notes(maintenanceDTO.getNotes())
                    .location(maintenanceDTO.getLocation())
                    .createdBy(createdBy)   // ← new
                    .updatedBy(createdBy)   // ← new (same on creation)
                    .build();

            return maintenanceRepositories.save(maintenance);

        } catch (InventoryException e) {
            throw e;
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation creating maintenance", e);
            throw new InventoryException(
                    "Failed to create ticket — data integrity error.",
                    HttpStatus.CONFLICT);
        } catch (Exception e) {
            log.error("Failed to create maintenance", e);
            throw new InventoryException(
                    "Failed to create Maintenance",
                    HttpStatus.INTERNAL_SERVER_ERROR);
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
    public Maintenance updateMaintenanceById(Long id, MaintenanceDTO maintenanceDTO) {

        try {
            Maintenance maintenance = maintenanceRepositories.findById(id)
                    .orElseThrow(() -> new InventoryException(
                            "Maintenance not found", HttpStatus.NOT_FOUND));

            Asset asset = assetRepositories.findById(maintenanceDTO.getAssetId())
                    .orElseThrow(() -> new InventoryException(
                            "Asset not found", HttpStatus.NOT_FOUND));

            boolean hasActiveTicket =
                    maintenanceRepositories.existsByAsset_IdAndStatusNotInAndIdNot(
                            maintenanceDTO.getAssetId(), CLOSED_STATUSES, id);

            if (hasActiveTicket) {
                throw new InventoryException(
                        "This asset already has another active maintenance ticket.",
                        HttpStatus.CONFLICT);
            }

            // ── Resolve the updating user ──────────────────────────────────
            User updatedBy = resolveUser(maintenanceDTO.getCurrentUserId());

            maintenance.setAsset(asset);
            maintenance.setIssueTitle(maintenanceDTO.getIssueTitle());
            maintenance.setDescription(maintenanceDTO.getDescription());
            maintenance.setPriority(maintenanceDTO.getPriority());
            maintenance.setStatus(maintenanceDTO.getStatus());
            maintenance.setReportedDate(maintenanceDTO.getReportedDate());
            maintenance.setDueDate(maintenanceDTO.getDueDate());
            maintenance.setAssignedTo(maintenanceDTO.getAssignedTo());
            maintenance.setCost(maintenanceDTO.getCost());
            maintenance.setNotes(maintenanceDTO.getNotes());
            maintenance.setLocation(maintenanceDTO.getLocation());
            maintenance.setUpdatedBy(updatedBy);   // ← new (createdBy stays untouched)

            return maintenanceRepositories.save(maintenance);

        } catch (InventoryException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to update maintenance with id {}", id, e);
            throw new InventoryException(
                    "Failed to update Maintenance",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ── all other methods unchanged ────────────────────────────────────────

    private User resolveUser(Long userId) {
        if (userId == null) {
            throw new InventoryException("Current user context is missing", HttpStatus.UNAUTHORIZED);
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new InventoryException("User not found", HttpStatus.NOT_FOUND));
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

    @Override
    public Page<Maintenance> getAssetsByLocation(String locationName,Pageable pageable) {
        try {
            return maintenanceRepositories
                    .findByLocationContainingIgnoreCase(locationName.trim(),pageable);
        } catch (Exception e) {
            log.error("Failed to get assets for location: {}", locationName, e);
            throw new InventoryException("Failed to get assets", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Page<Maintenance> getAllMaintenanceFiltered(
            Pageable pageable,
            String search,
            String status,
            String priority,
            String location) {

        // Convert frontend labels → backend DB enum names
        // Frontend sends "OPEN", "IN_PROGRESS" etc (already converted in fetchMaintenancePage)
        // We just null-out blanks
        String resolvedSearch   = (search   != null && !search.isBlank())   ? search.trim()    : null;
        String resolvedStatus   = (status   != null && !status.isBlank())   ? status.trim()    : null;
        String resolvedPriority = (priority != null && !priority.isBlank()) ? priority.trim()  : null;
        String resolvedLocation = (location != null && !location.isBlank()) ? location.trim()  : null;

        try {
            return maintenanceRepositories.findAllFiltered(
                    resolvedSearch,
                    resolvedStatus,
                    resolvedPriority,
                    resolvedLocation,
                    pageable
            );
        } catch (Exception e) {
            log.error("Failed to fetch filtered maintenance", e);
            throw new InventoryException(
                    "Failed to fetch maintenance records",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}