package com.cic.inventory.services.impl;


import com.cic.inventory.dtos.AssetTransferDTO;
import com.cic.inventory.entities.Asset;
import com.cic.inventory.entities.AssetTransfer;
import com.cic.inventory.entities.Employee;
import com.cic.inventory.entities.Location;
import com.cic.inventory.exceptions.InventoryException;
import com.cic.inventory.repositories.AssetRepositories;
import com.cic.inventory.repositories.AssetTransferRepositories;
import com.cic.inventory.repositories.EmployeeRepositories;
import com.cic.inventory.repositories.LocationRepositories;
import com.cic.inventory.services.AssetTransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssetTransferServiceImpl implements AssetTransferService {

    private final AssetTransferRepositories assetTransferRepositories;
    private final AssetRepositories assetRepositories;
    private final EmployeeRepositories employeeRepositories;
    private final LocationRepositories locationRepositories;
    private final ModelMapper modelMapper;

    @Override
    public AssetTransfer createNewAssetTransfer(AssetTransferDTO dto) {
        try {
            // ── Resolve asset ─────────────────────────────────────────────────
            Asset asset = assetRepositories.findById(dto.getAssetId().getId())
                    .orElseThrow(() -> new InventoryException("Asset not found", HttpStatus.NOT_FOUND));

            // ── Resolve optional from/to employees ───────────────────────────
            Employee fromEmployee = resolveEmployee(dto.getFromEmployeeId());
            Employee toEmployee   = resolveEmployee(dto.getToEmployeeId());

            // ── Resolve optional from/to locations ───────────────────────────
            Location fromLocation = resolveLocation(dto.getFromLocationId());
            Location toLocation   = resolveLocation(dto.getToLocationId());

            // ── Build and persist the transfer record ─────────────────────────
            AssetTransfer transfer = new AssetTransfer();
            transfer.setAsset(asset);
            transfer.setTransferType(dto.getTransferType());
            transfer.setTransferDate(dto.getTransferDate());
            transfer.setReason(dto.getReason());
            transfer.setFromEmployee(fromEmployee);
            transfer.setToEmployee(toEmployee);
            transfer.setFromLocation(fromLocation);
            transfer.setToLocation(toLocation);

            AssetTransfer saved = assetTransferRepositories.save(transfer);
            log.info("Asset transfer created — id={}, asset={}, type={}",
                    saved.getId(), asset.getAssetCode(), dto.getTransferType());
            return saved;

        } catch (InventoryException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to create asset transfer", e);
            throw new InventoryException("Failed to create Asset Transfer", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Page<AssetTransfer> getAllAssetTransfers(Pageable pageable) {
        try {
            return assetTransferRepositories.findAll(pageable);
        } catch (Exception e) {
            log.error("Failed to fetch asset transfers", e);
            throw new InventoryException("Failed to fetch Asset Transfers", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public AssetTransfer getAssetTransferById(Long id) {
        return assetTransferRepositories.findById(id)
                .orElseThrow(() -> {
                    log.warn("Asset transfer not found with id={}", id);
                    return new InventoryException("Asset Transfer not found", HttpStatus.NOT_FOUND);
                });
    }

    @Override
    public AssetTransfer updateAssetTransferById(Long id, AssetTransfer updated) {
        try {
            AssetTransfer existing = assetTransferRepositories.findById(id)
                    .orElseThrow(() -> new InventoryException("Asset transfer not found", HttpStatus.NOT_FOUND));

            Asset asset = assetRepositories.findById(updated.getAsset().getId())
                    .orElseThrow(() -> new InventoryException("Asset not found", HttpStatus.NOT_FOUND));

            // Only update mutable fields — never overwrite the audit trail
            existing.setTransferType(updated.getTransferType());
            existing.setTransferDate(updated.getTransferDate());
            existing.setReason(updated.getReason());
            existing.setAsset(asset);

            return assetTransferRepositories.save(existing);

        } catch (InventoryException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to update asset transfer id={}", id, e);
            throw new InventoryException("Failed to update Asset Transfer", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void deleteAssetTransfer(Long id) {
        try {
            assetTransferRepositories.deleteById(id);
        } catch (Exception e) {
            log.error("Failed to delete asset transfer id={}", id, e);
            throw new InventoryException("Failed to delete asset transfer", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private Employee resolveEmployee(AssetTransferDTO.EmployeeRef ref) {
        if (ref == null || ref.getId() == null) return null;
        return employeeRepositories.findById(ref.getId())
                .orElseThrow(() -> new InventoryException(
                        "Employee not found with id: " + ref.getId(), HttpStatus.NOT_FOUND));
    }

    private Location resolveLocation(AssetTransferDTO.LocationRef ref) {
        if (ref == null || ref.getId() == null) return null;
        return locationRepositories.findById(ref.getId())
                .orElseThrow(() -> new InventoryException(
                        "Location not found with id: " + ref.getId(), HttpStatus.NOT_FOUND));
    }
}
