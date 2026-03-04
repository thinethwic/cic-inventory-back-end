package com.cic.inventory.services.impl;

import com.cic.inventory.dtos.AssetDTO;
import com.cic.inventory.entities.Asset;
import com.cic.inventory.exceptions.InventoryException;
import com.cic.inventory.repositories.AssetRepositories;
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

    @Override
    public Asset createNewAsset(AssetDTO assetDTO) {
        try {
            Asset asset = modelMapper.map(assetDTO,Asset.class);
            return assetRepositories.save(asset);

        } catch (DataIntegrityViolationException e){
            log.error("Data integrity violation while creating asset: {}", e.getMessage());
            throw new InventoryException("Asset with this serial and asset code already exists", HttpStatus.CONFLICT);
        } catch (Exception exception) {
            log.error("Failed to create new asset", exception);
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
            Asset asset =  assetRepositories.findById(id).orElseThrow(
                    () -> new InventoryException("Asset Not Found", HttpStatus.NOT_FOUND)
            );
            modelMapper.map(updatedAsset, asset);
            return assetRepositories.save(asset);
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
