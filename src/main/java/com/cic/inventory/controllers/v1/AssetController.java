package com.cic.inventory.controllers.v1;

import com.cic.inventory.controllers.AbstractController;
import com.cic.inventory.dtos.AssetDTO;
import com.cic.inventory.entities.Asset;
import com.cic.inventory.services.AssetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/v1/assets")
@RequiredArgsConstructor
public class AssetController extends AbstractController {
    private final AssetService assetService;
    private final ModelMapper modelMapper;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<Asset>> getAllAssets(Pageable pageable) {
        Page<Asset> mentors = assetService.getAllAsset(pageable);
        return sendOkResponse(mentors);
    }

    @GetMapping("{id}")
    public ResponseEntity<Asset> getAssetById(@PathVariable Long id) {
        Asset asset = assetService.getAssetById(id);
        return sendOkResponse(asset);
    }

    @PostMapping
//    @PreAuthorize("hasAnyRole('admin')")
    public ResponseEntity<Asset> createAsset(@Validated @RequestBody AssetDTO assetDTO) {
        Asset asset = assetService.createNewAsset(assetDTO);
        return sendCreatedResponse(asset);
    }

    @PutMapping("{id}")
//    @PreAuthorize("hasAnyAuthority('admin', 'ROLE_admin')")
    public ResponseEntity<Asset> updateAsset(@PathVariable Long id, @Valid @RequestBody AssetDTO updatedAssetDTO) {
        Asset asset = modelMapper.map(updatedAssetDTO, Asset.class);
        Asset updatedAsset = assetService.updateAssetById(id, asset);
        return sendOkResponse(updatedAsset);

    }

    @DeleteMapping("{id}")
//    @PreAuthorize("hasAnyRole('admin')")
    public ResponseEntity<Asset> deleteAsset(@PathVariable Long id) {
        assetService.deleteAsset(id);
        return sendNoContentResponse();
    }


}
