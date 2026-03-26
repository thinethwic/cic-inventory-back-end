package com.cic.inventory.controllers.v1;

import com.cic.inventory.controllers.AbstractController;
import com.cic.inventory.dtos.AssetDTO;
import com.cic.inventory.dtos.responses.AssetResponseDTO;
import com.cic.inventory.entities.Asset;
import com.cic.inventory.security.UserPrincipal;
import com.cic.inventory.services.AssetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.cic.inventory.constants.UserRoles.ROLE_admin;
import static com.cic.inventory.constants.UserRoles.ROLE_admin_user;

@RestController
@RequestMapping(path = "/api/v1/assets")
@RequiredArgsConstructor
public class AssetController extends AbstractController {
    private final AssetService assetService;
    private final ModelMapper modelMapper;

    @GetMapping
    public ResponseEntity<Page<AssetResponseDTO>> getAllAssets(
            Pageable pageable,
            Authentication authentication
    ) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        // Safely check for admin roles with or without the "ROLE_" prefix
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .anyMatch(role ->
                        role.equalsIgnoreCase("ROLE_Admin") ||
                                role.equalsIgnoreCase("Admin") ||
                                role.equalsIgnoreCase("ROLE_admin_user") ||
                                role.equalsIgnoreCase("admin_user")
                );

        if (isAdmin) {
            // Admin → all assets, no location filter from JWT
            return sendOkResponse(assetService.getAllAsset(pageable));
        }

        // Normal user → filter by location from JWT
        String location = principal.getLocation() != null
                ? principal.getLocation().trim()
                : "";

        if (location.isEmpty()) {
            return sendOkResponse(Page.empty(pageable));
        }

        return sendOkResponse(assetService.getAssetsByLocation(location, pageable));
    }

    @GetMapping("{id}")
    public ResponseEntity<AssetResponseDTO> getAssetById(@PathVariable Long id) {
        AssetResponseDTO asset = assetService.getAssetById(id);
        return sendOkResponse(asset);
    }

    @PostMapping
    public ResponseEntity<Asset> createAsset(@Validated @RequestBody AssetDTO assetDTO) {
        Asset asset = assetService.createNewAsset(assetDTO);
        return sendCreatedResponse(asset);
    }

    @PutMapping("{id}")
    public ResponseEntity<Asset> updateAsset(
            @PathVariable Long id,
            @Valid @RequestBody AssetDTO assetDTO
    ) {
        Asset updatedAsset = assetService.updateAssetById(id, assetDTO);
        return sendOkResponse(updatedAsset);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Asset> deleteAsset(@PathVariable Long id) {
        assetService.deleteAsset(id);
        return sendNoContentResponse();
    }

    @GetMapping("/scan/{code}")
    public ResponseEntity<Asset> scanAsset(@PathVariable String code) {
        Asset asset = assetService.findByScan(code);
        return sendOkResponse(asset);
    }


}
