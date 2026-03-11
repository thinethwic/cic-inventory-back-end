package com.cic.inventory.services.impl;

import com.cic.inventory.dtos.AssetDTO;
import com.cic.inventory.entities.Asset;
import com.cic.inventory.entities.Department;
import com.cic.inventory.entities.Employee;
import com.cic.inventory.entities.Location;
import com.cic.inventory.exceptions.InventoryException;
import com.cic.inventory.repositories.AssetRepositories;
import com.cic.inventory.repositories.EmployeeRepositories;
import com.cic.inventory.repositories.LocationRepositories;
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

    @Override
    public Asset createNewAsset(AssetDTO assetDTO) {
        try {
            Location location = locationRepositories.findById(assetDTO.getLocationId())
                    .orElseThrow(() -> new InventoryException("Location not found", HttpStatus.NOT_FOUND));

            Employee employee = null;
            if (assetDTO.getAssignedToId() != null) {
                employee = employeeRepositories.findById(assetDTO.getAssignedToId())
                        .orElseThrow(() -> new InventoryException("Employee not found", HttpStatus.NOT_FOUND));
            }

            Asset asset = modelMapper.map(assetDTO, Asset.class);
            asset.setLocation(location);
            asset.setAssignedTo(employee);

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
    public Page<Asset> getAllAsset(Pageable pageable) {
        try {
            return assetRepositories.findAll(pageable);
        } catch (Exception exception) {
            log.error("Failed to get all assets", exception);
            throw new InventoryException("Failed to get all assets", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Asset getAssetById(Long id) {
        try {
            Asset asset =  assetRepositories.findById(id).orElseThrow(
                    () -> new InventoryException("Asset Not Found", HttpStatus.NOT_FOUND)
            );
            log.info("Successfully fetched asset {}", id);
            return asset;
        }catch (InventoryException inventoryException){
            log.warn("Asset not found with id: {} to fetch", id, inventoryException);
            throw new InventoryException("Asset Not found", HttpStatus.NOT_FOUND);
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

            modelMapper.map(assetUpdateDTO, existing);

            existing.setLocation(location);
            existing.setAssignedTo(employee);

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

}
