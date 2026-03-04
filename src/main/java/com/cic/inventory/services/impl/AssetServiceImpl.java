package com.cic.inventory.services.impl;

import com.cic.inventory.entities.Asset;
import com.cic.inventory.services.AssetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor

public class AssetServiceImpl implements AssetService {
    @Override
    public Asset createNewAsset(Asset asset) {
        return null;
    }

    @Override
    public List<Asset> getAllAsset() {
        return List.of();
    }

    @Override
    public Asset getAssetById(Long id) {
        return null;
    }

    @Override
    public Asset updateAssetById(Long id, Asset updatedAsset) {
        return null;
    }

    @Override
    public void deleteAsset(Long id) {

    }
}
