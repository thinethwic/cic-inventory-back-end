package com.cic.inventory.services.impl;


import com.cic.inventory.dtos.AssetTransferDTO;
import com.cic.inventory.entities.Asset;
import com.cic.inventory.entities.AssetTransfer;
import com.cic.inventory.exceptions.InventoryException;
import com.cic.inventory.repositories.AssetRepositories;
import com.cic.inventory.repositories.AssetTransferRepositories;
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
    private final ModelMapper modelMapper;
    private final AssetRepositories assetRepositories;
    @Override
    public AssetTransfer createNewAssetTransfer(AssetTransferDTO assetTransferDTO) {
        try{
            Asset asset = assetRepositories.findById(assetTransferDTO.getAssetId().getId())
                    .orElseThrow(() -> new InventoryException("Asset not found", HttpStatus.NOT_FOUND));

            AssetTransfer assetTransfer = modelMapper.map(assetTransferDTO, AssetTransfer.class);
            assetTransfer.setAsset(asset);

            return assetTransferRepositories.save(assetTransfer);
        }catch (InventoryException inventoryException){
            throw new InventoryException("Asset Not Found",HttpStatus.NOT_FOUND);
        } catch (Exception exception) {
            throw new InventoryException("Failed to create Asset Transfer", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Page<AssetTransfer> getAllAssetTransfers(Pageable pageable) {
        try {
            return assetTransferRepositories.findAll(pageable);
        }catch (Exception exception){
            throw new InventoryException("Failed to All Asset Transfers",HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public AssetTransfer getAssetTransferById(Long id) {
        try {
            return assetTransferRepositories.findById(id).orElseThrow(
                    () -> new InventoryException("Asset Transfer Not Found", HttpStatus.NOT_FOUND)
            );
        }catch (InventoryException inventoryException){
            log.warn("Asset Transfer not found with id: {} to fetch", id, inventoryException);
            throw new InventoryException("Asset Transfer Not found", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public AssetTransfer updateAssetTransferById(Long id, AssetTransfer updatedAssetTransfer) {
        try {
            AssetTransfer assetTransfer = assetTransferRepositories.findById(id)
                    .orElseThrow(() -> new InventoryException("Asset transfer not found", HttpStatus.NOT_FOUND));

            Asset asset = assetRepositories.findById(updatedAssetTransfer.getAsset().getId())
                    .orElseThrow(() -> new InventoryException("Asset not found", HttpStatus.NOT_FOUND));

            modelMapper.map(updatedAssetTransfer, assetTransfer);
            assetTransfer.setAsset(asset);
            return assetTransferRepositories.save(assetTransfer);

        }catch (InventoryException inventoryException){
            throw new InventoryException("Asset and Asset Transfer not found", HttpStatus.NOT_FOUND);
        }catch (Exception exception){
            throw new InventoryException("Failed to update Asset Transfer", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void deleteAssetTransfer(Long id) {
        try {
            assetTransferRepositories.deleteById(id);
        } catch (Exception exception) {
            log.error("Failed to delete asset transfer with id {}", id, exception);
            throw new InventoryException("Failed to delete asset transfer", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
