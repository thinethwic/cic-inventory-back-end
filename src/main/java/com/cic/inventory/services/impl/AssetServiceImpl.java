package com.cic.inventory.services.impl;

import com.cic.inventory.dtos.AssetDTO;
import com.cic.inventory.dtos.responses.AssetResponseDTO;
import com.cic.inventory.entities.*;
import com.cic.inventory.exceptions.InventoryException;
import com.cic.inventory.repositories.AssetRepositories;
import com.cic.inventory.repositories.EmployeeRepositories;
import com.cic.inventory.repositories.LocationRepositories;
import com.cic.inventory.repositories.SupplierRepositories;
import com.cic.inventory.services.AssetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {

    private final AssetRepositories assetRepositories;
    private final ModelMapper modelMapper;
    private final EmployeeRepositories employeeRepositories;
    private final LocationRepositories locationRepositories;
    private final SupplierRepositories supplierRepositories;

    @Override
    public Asset createNewAsset(AssetDTO assetDTO) {
        try {
            Location location = locationRepositories.findById(assetDTO.getLocationId())
                    .orElseThrow(() -> new InventoryException("Location not found", HttpStatus.NOT_FOUND));

            Supplier supplier = supplierRepositories.findById(assetDTO.getSupplierId())
                    .orElseThrow(() -> new InventoryException("Supplier not found", HttpStatus.NOT_FOUND));

            Employee employee = null;
            if (assetDTO.getAssignedToId() != null) {
                employee = employeeRepositories.findById(assetDTO.getAssignedToId())
                        .orElseThrow(() -> new InventoryException("Employee not found", HttpStatus.NOT_FOUND));
            }

            Asset asset = modelMapper.map(assetDTO, Asset.class);
            asset.setLocation(location);
            asset.setAssignedTo(employee);
            asset.setSupplier(supplier);

            return assetRepositories.save(asset);

        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation while creating asset: {}", e.getMessage());
            throw new InventoryException("Asset with this serial number or asset code already exists", HttpStatus.CONFLICT);
        } catch (InventoryException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to create new asset", e);
            throw new InventoryException("Failed to create new asset", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Page<AssetResponseDTO> getAllAsset(Pageable pageable) {
        try {
            return assetRepositories.findAll(pageable).map(this::toResponse);
        } catch (Exception e) {
            log.error("Failed to get all assets", e);
            throw new InventoryException("Failed to get all assets", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Page<AssetResponseDTO> getAssetsByLocation(String locationName, Pageable pageable) {
        try {
            return assetRepositories
                    .findByLocation_NameIgnoreCase(locationName, pageable)
                    .map(this::toResponse);
        } catch (Exception e) {
            log.error("Failed to get assets for location: {}", locationName, e);
            throw new InventoryException("Failed to get assets", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Page<AssetResponseDTO> getAssetsByDepartment(String departmentName, Pageable pageable) {
        try {
            return assetRepositories
                    .findByAssignedTo_Department_NameIgnoreCase(departmentName, pageable)
                    .map(this::toResponse);
        } catch (Exception e) {
            log.error("Failed to get assets for department: {}", departmentName, e);
            throw new InventoryException("Failed to get assets", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public AssetResponseDTO getAssetById(Long id) {
        try {
            Asset asset = assetRepositories.findById(id)
                    .orElseThrow(() -> new InventoryException("Asset Not Found", HttpStatus.NOT_FOUND));
            log.info("Successfully fetched asset {}", id);
            return toResponse(asset);
        } catch (InventoryException e) {
            log.warn("Asset not found with id: {}", id, e);
            throw e;
        }
    }

    @Override
    public Asset updateAssetById(Long id, AssetDTO assetUpdateDTO) {
        try {
            Asset existing = assetRepositories.findById(id)
                    .orElseThrow(() -> new InventoryException("Asset not found", HttpStatus.NOT_FOUND));

            Employee employee = null;
            if (assetUpdateDTO.getAssignedToId() != null) {
                employee = employeeRepositories.findById(assetUpdateDTO.getAssignedToId())
                        .orElseThrow(() -> new InventoryException("Employee not found", HttpStatus.NOT_FOUND));
            }

            Location location = locationRepositories.findById(assetUpdateDTO.getLocationId())
                    .orElseThrow(() -> new InventoryException("Location not found", HttpStatus.NOT_FOUND));

            Supplier supplier = supplierRepositories.findById(assetUpdateDTO.getSupplierId())
                    .orElseThrow(() -> new InventoryException("Supplier not found", HttpStatus.NOT_FOUND));

            modelMapper.map(assetUpdateDTO, existing);

            existing.setLocation(location);
            existing.setAssignedTo(employee);
            existing.setSupplier(supplier);

            return assetRepositories.save(existing);

        } catch (InventoryException e) {
            throw e;
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation while updating asset: {}", e.getMessage());
            throw new InventoryException("Asset code or serial number already exists", HttpStatus.CONFLICT);
        } catch (Exception e) {
            log.error("Error updating asset", e);
            throw new InventoryException("Failed to update asset", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @Override
    public void deleteAsset(Long id) {
        try {
            assetRepositories.deleteById(id);
        } catch (DataIntegrityViolationException dataIntegrityViolationException){
            throw new InventoryException( "Failed to delete asset! This asset is linked to other records.",
                    HttpStatus.CONFLICT);
        } catch (Exception exception) {
            log.error("Failed to delete asset with id {}", id, exception);
            throw new InventoryException("Failed to delete asset", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    public Asset findByScan(String code) {
        String trimmed = code.trim();

        // Search by barcode first, then serial number, then asset code
        return assetRepositories.findByBarcode(trimmed)
                .or(() -> assetRepositories.findBySerialNo(trimmed))
                .or(() -> assetRepositories.findByAssetCode(trimmed))
                .orElseThrow(() -> new InventoryException(
                        "Asset not found for scan code: " , HttpStatus.NOT_FOUND));
    }

    // ── Mapping helper ────────────────────────────────────────────────────────

    private AssetResponseDTO toResponse(Asset asset) {
        AssetResponseDTO dto = new AssetResponseDTO();
        dto.setId(asset.getId());
        dto.setAssetCode(asset.getAssetCode());
        dto.setBarcode(asset.getBarcode());
        dto.setCategory(asset.getCategory());
        dto.setBrand(asset.getBrand());
        dto.setModel(asset.getModel());
        dto.setSerialNo(asset.getSerialNo());
        dto.setStatus(asset.getStatus());
        dto.setPurchaseDate(asset.getPurchaseDate());
        dto.setWarrantyEnd(asset.getWarrantyEnd());
        dto.setQrCode(asset.getQrCode());
        dto.setCreatedAt(asset.getCreatedAt());
        dto.setUpdatedAt(asset.getUpdatedAt());

        // Location — name string + ID
        if (asset.getLocation() != null) {
            dto.setLocation(asset.getLocation().getName());
            dto.setLocationId(asset.getLocation().getId());   // ← ADD THIS
        }

        // Employee — "empId - name" string + ID
        if (asset.getAssignedTo() != null) {
            Employee emp = asset.getAssignedTo();
            dto.setAssignedTo(emp.getEmpId() + " - " + emp.getName());
            dto.setAssignedToId(emp.getId());                 // ← ADD THIS
        }

        // Supplier
        if (asset.getSupplier() != null) {
            dto.setSupplierId(asset.getSupplier().getId());
            dto.setSupplierName(asset.getSupplier().getName());
        }

        return dto;
    }

}
