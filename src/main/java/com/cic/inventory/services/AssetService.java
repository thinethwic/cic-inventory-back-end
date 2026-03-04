package com.cic.inventory.services;

import com.cic.inventory.entities.Asset;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface AssetService {
    Asset createNewAsset(Asset asset);
    List<Asset> getAllAsset();
    Asset getAssetById(Long id);
    Asset updateAssetById(Long id, Asset updatedAsset);
    void deleteAsset(Long id);
}
