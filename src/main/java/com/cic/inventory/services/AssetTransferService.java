package com.cic.inventory.services;

import com.cic.inventory.dtos.AssetDTO;
import com.cic.inventory.dtos.AssetTransferDTO;
import com.cic.inventory.entities.Asset;
import com.cic.inventory.entities.AssetTransfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AssetTransferService {
    AssetTransfer createNewAssetTransfer(AssetTransferDTO assetTransferDTO);
    Page<AssetTransfer> getAllAssetTransfers(Pageable pageable);
    AssetTransfer getAssetTransferById(Long id);
    AssetTransfer updateAssetTransferById(Long id, AssetTransfer updatedAssetTransfer);
    void deleteAssetTransfer(Long id);
}
