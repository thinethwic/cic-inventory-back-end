package com.cic.inventory.controllers.v1;

import com.cic.inventory.controllers.AbstractController;
import com.cic.inventory.dtos.AssetDTO;
import com.cic.inventory.dtos.responses.AssetResponseDTO;
import com.cic.inventory.entities.Asset;
import com.cic.inventory.security.UserPrincipal;
import com.cic.inventory.services.AssetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/assets")
@RequiredArgsConstructor
@Slf4j
public class AssetController extends AbstractController {

    private final AssetService assetService;

    @GetMapping
    // ✅ No @PreAuthorize — manual role check inside handles both admin and normal user
    public ResponseEntity<Page<AssetResponseDTO>> getAllAssets(
            Pageable pageable,
            Authentication authentication
    ) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        // Matches "ROLE_Admin" set by AuthenticationFilter from roles array ["Admin"]
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equalsIgnoreCase("ROLE_Admin") ||
                        a.getAuthority().equalsIgnoreCase("ROLE_admin_user"));

        if (isAdmin) {
            return sendOkResponse(assetService.getAllAsset(pageable));
        }

        String departmentName = principal.getDepartmentName() != null
                ? principal.getDepartmentName().trim()
                : "";

        String location = principal.getLocation() != null
                ? principal.getLocation().trim()
                : "";

// ← ADD THIS
        log.info("Non-admin access → departmentName: '{}', location: '{}'", departmentName, location);

        if (!departmentName.isEmpty()) {
            log.info("Filtering by department: '{}'", departmentName);
            return sendOkResponse(assetService.getAssetsByDepartment(departmentName, pageable));
        }

        if (!location.isEmpty()) {
            log.info("Filtering by location: '{}'", location);
            return sendOkResponse(assetService.getAssetsByLocation(location, pageable));
        }

        return sendOkResponse(Page.empty(pageable));
    }

    @GetMapping("{id}")
    public ResponseEntity<AssetResponseDTO> getAssetById(@PathVariable Long id) {
        return sendOkResponse(assetService.getAssetById(id));
    }

    @PostMapping
    public ResponseEntity<Asset> createAsset(@Validated @RequestBody AssetDTO assetDTO) {
        return sendCreatedResponse(assetService.createNewAsset(assetDTO));
    }

    @PutMapping("{id}")
    public ResponseEntity<Asset> updateAsset(
            @PathVariable Long id,
            @Valid @RequestBody AssetDTO assetDTO
    ) {
        return sendOkResponse(assetService.updateAssetById(id, assetDTO));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Asset> deleteAsset(@PathVariable Long id) {
        assetService.deleteAsset(id);
        return sendNoContentResponse();
    }

    @GetMapping("/scan/{code}")
    public ResponseEntity<Asset> scanAsset(@PathVariable String code) {
        return sendOkResponse(assetService.findByScan(code));
    }
}