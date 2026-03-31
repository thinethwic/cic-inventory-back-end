package com.cic.inventory.services;

import com.cic.inventory.dtos.AssetDTO;
import com.cic.inventory.dtos.responses.AssetResponseDTO;
import com.cic.inventory.entities.Asset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface AssetService {
    Asset createNewAsset(AssetDTO assetDTO);
    Page<AssetResponseDTO> getAllAsset(Pageable pageable);
    AssetResponseDTO getAssetById(Long id);
    Asset updateAssetById(Long id,AssetDTO assetUpdateDTO);
    void deleteAsset(Long id);
    Asset findByScan(String code);

    Page<AssetResponseDTO> getAssetsByLocation(String locationName, Pageable pageable);
    Page<AssetResponseDTO> getAssetsByDepartment(String departmentName, Pageable pageable);
}
