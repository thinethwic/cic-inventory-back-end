package com.cic.inventory.controllers.v1;

import com.cic.inventory.controllers.AbstractController;
import com.cic.inventory.dtos.AssetDTO;
import com.cic.inventory.dtos.responses.AssetAttachmentDTO;
import com.cic.inventory.dtos.responses.AssetBreakdownDTO;
import com.cic.inventory.dtos.responses.AssetResponseDTO;
import com.cic.inventory.dtos.responses.DashboardStatsDTO;
import com.cic.inventory.entities.Asset;
import com.cic.inventory.entities.AssetAttachment;
import com.cic.inventory.security.UserPrincipal;
import com.cic.inventory.services.AssetAttachmentService;
import com.cic.inventory.services.AssetService;
import com.cic.inventory.services.FileStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping(path = "/api/v1/assets")
@RequiredArgsConstructor
@Slf4j
public class AssetController extends AbstractController {

    private final AssetService assetService;
    private final AssetAttachmentService assetAttachmentService;
    private final FileStorageService fileStorageService;

    @GetMapping
    // ✅ No @PreAuthorize — manual role check inside handles both admin and normal user
    public ResponseEntity<Page<AssetResponseDTO>> getAllAssets(
            Pageable pageable,
            Authentication authentication,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String supplier
    ) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        // Matches "ROLE_Admin" set by AuthenticationFilter from roles array ["Admin"]
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equalsIgnoreCase("ROLE_Admin") ||
                        a.getAuthority().equalsIgnoreCase("ROLE_admin_user"));

        if (isAdmin) {
            return sendOkResponse(assetService.getAllAsset(pageable, search, status, category, location, supplier));
        }

        String departmentName = (principal.getDepartmentName() == null || principal.getDepartmentName().isBlank())
                ? null
                : principal.getDepartmentName().trim();

        String scopeLocation = (principal.getLocation() == null || principal.getLocation().isBlank())
                ? null
                : principal.getLocation().trim();

        log.info("Non-admin access → departmentName: '{}', location: '{}'", departmentName, scopeLocation);

        return sendOkResponse(
                assetService.getAssetsByAccessScope(scopeLocation, departmentName, pageable, search, status, category, supplier)
        );
    }

    // Declared before "{id}" is irrelevant to Spring's matching (literal path
    // segments always win over a path-variable segment), but the endpoint
    // must stay a single static segment — "/assets/dashboard-stats" — so it
    // can never be mistaken for "/assets/{id}".
    @GetMapping("dashboard-stats")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equalsIgnoreCase("ROLE_Admin") ||
                        a.getAuthority().equalsIgnoreCase("ROLE_admin_user"));

        if (isAdmin) {
            return sendOkResponse(assetService.getDashboardStats(null, null));
        }

        String departmentName = (principal.getDepartmentName() == null || principal.getDepartmentName().isBlank())
                ? null
                : principal.getDepartmentName().trim();

        String scopeLocation = (principal.getLocation() == null || principal.getLocation().isBlank())
                ? null
                : principal.getLocation().trim();

        if (scopeLocation == null) {
            return sendOkResponse(new DashboardStatsDTO(0, 0, 0, 0, List.of()));
        }

        return sendOkResponse(assetService.getDashboardStats(scopeLocation, departmentName));
    }

    // Same single-static-segment reasoning as "dashboard-stats" above. Used by
    // the (admin-only) Reports page so its charts/KPIs render from a small
    // aggregate query instead of waiting on a fetch of every asset row.
    @GetMapping("breakdown")
    public ResponseEntity<AssetBreakdownDTO> getAssetBreakdown() {
        return sendOkResponse(assetService.getAssetBreakdown());
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

    @PostMapping(value = "/{id}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AssetAttachmentDTO> uploadAttachment(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file
    ) {
        return sendCreatedResponse(assetAttachmentService.upload(id, file));
    }

    @GetMapping("/{id}/attachments")
    public ResponseEntity<List<AssetAttachmentDTO>> listAttachments(@PathVariable Long id) {
        return sendOkResponse(assetAttachmentService.list(id));
    }

    @GetMapping("/{id}/attachments/{attachmentId}/download")
    public ResponseEntity<Resource> downloadAttachment(
            @PathVariable Long id,
            @PathVariable Long attachmentId
    ) {
        return serveAttachment(id, attachmentId, ContentDisposition.attachment());
    }

    @GetMapping("/{id}/attachments/{attachmentId}/view")
    public ResponseEntity<Resource> viewAttachment(
            @PathVariable Long id,
            @PathVariable Long attachmentId
    ) {
        return serveAttachment(id, attachmentId, ContentDisposition.inline());
    }

    @DeleteMapping("/{id}/attachments/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(
            @PathVariable Long id,
            @PathVariable Long attachmentId
    ) {
        assetAttachmentService.delete(id, attachmentId);
        return sendNoContentResponse();
    }

    private ResponseEntity<Resource> serveAttachment(
            Long assetId,
            Long attachmentId,
            ContentDisposition.Builder dispositionBuilder
    ) {
        AssetAttachment attachment = assetAttachmentService.getForAccess(assetId, attachmentId);
        Resource resource = fileStorageService.loadAsResource(attachment.getFilePath());

        MediaType mediaType = attachment.getContentType() != null
                ? MediaType.parseMediaType(attachment.getContentType())
                : MediaType.APPLICATION_OCTET_STREAM;

        ContentDisposition disposition = dispositionBuilder
                .filename(attachment.getFileName())
                .build();

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(resource);
    }
}
