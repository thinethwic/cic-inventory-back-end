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
            Employee employee = employeeRepositories.findById(assetDTO.getAssignedToId())
                    .orElseThrow(() -> new InventoryException("Employee not found", HttpStatus.NOT_FOUND));

            Location location = locationRepositories.findById(assetDTO.getLocationId())
                    .orElseThrow(() -> new InventoryException("Location not found", HttpStatus.NOT_FOUND));

            Asset asset = modelMapper.map(assetDTO, Asset.class);
            asset.setAssignedTo(employee);   // ✅ set entities directly on the Asset
            asset.setLocation(location);

            return assetRepositories.save(asset);

        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation while creating asset: {}", e.getMessage());
            throw new InventoryException("Asset with this serial and asset code already exists", HttpStatus.CONFLICT);
        } catch (InventoryException e) {
            throw e;  // ✅ don't swallow your own exceptions
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
    public Asset updateAssetById(Long id, Asset updatedAsset) {
        try {
            Asset existing = assetRepositories.findById(id)
                    .orElseThrow(() -> new InventoryException("Asset not found", HttpStatus.NOT_FOUND));

            Employee employee = employeeRepositories.findById(id)
                    .orElseThrow(() -> new InventoryException("Employee not found", HttpStatus.NOT_FOUND));

            Location location = locationRepositories.findById(id)
                    .orElseThrow(() -> new InventoryException("Location not found", HttpStatus.NOT_FOUND));

            // Map scalar fields (assetCode, brand, model, etc.) onto existing entity
            modelMapper.map(updatedAsset, existing);

            // Override with resolved entities — modelMapper can't do this
            existing.setAssignedTo(employee);
            existing.setLocation(location);

            return assetRepositories.save(existing);
        }catch (InventoryException inventoryException){
            log.warn("Asset not found with id: {} to fetch", id, inventoryException);
            throw new InventoryException("Asset Not found", HttpStatus.NOT_FOUND);

        } catch (Exception exception) {
            log.error("Error updating asset", exception);
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

}
