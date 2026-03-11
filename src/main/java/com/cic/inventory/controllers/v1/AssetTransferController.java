package com.cic.inventory.controllers.v1;

import com.cic.inventory.controllers.AbstractController;
import com.cic.inventory.dtos.AssetDTO;
import com.cic.inventory.dtos.AssetTransferDTO;
import com.cic.inventory.entities.Asset;
import com.cic.inventory.entities.AssetTransfer;
import com.cic.inventory.services.AssetTransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/assetTransfers")
@RequiredArgsConstructor
public class AssetTransferController extends AbstractController {
    private final AssetTransferService assetTransferService;
    private final ModelMapper modelMapper;

    @GetMapping
    public ResponseEntity<Page<AssetTransfer>> getAllAssetTransfers(Pageable pageable) {
        Page<AssetTransfer> assetTransfers = assetTransferService.getAllAssetTransfers(pageable);
        return sendOkResponse(assetTransfers);
    }

    @GetMapping("{id}")
    public ResponseEntity<AssetTransfer> getAssetTransferById(@PathVariable Long id) {
        AssetTransfer assetTransfer = assetTransferService.getAssetTransferById(id);
        return sendOkResponse(assetTransfer);
    }

    @PostMapping
    public ResponseEntity<AssetTransfer> createAssetTransfer(@Validated @RequestBody AssetTransferDTO assetDTO) {
        AssetTransfer assetTransfer = assetTransferService.createNewAssetTransfer(assetDTO);
        return sendCreatedResponse(assetTransfer);
    }

    @PutMapping("{id}")
    public ResponseEntity<AssetTransfer> updateAssetTransfer(@PathVariable Long id, @Valid @RequestBody AssetTransferDTO updatedAssetTransferDTO) {
        AssetTransfer assetTransfer = modelMapper.map(updatedAssetTransferDTO, AssetTransfer.class);
        AssetTransfer updatedAssetTransferById = assetTransferService.updateAssetTransferById(id, assetTransfer);
        return sendOkResponse(updatedAssetTransferById);

    }

    @DeleteMapping("{id}")
    public ResponseEntity<AssetTransfer> deleteAssetTransfer(@PathVariable Long id) {
        assetTransferService.deleteAssetTransfer(id);
        return sendNoContentResponse();
    }
}
