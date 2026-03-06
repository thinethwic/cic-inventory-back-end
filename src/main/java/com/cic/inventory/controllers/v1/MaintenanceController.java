package com.cic.inventory.controllers.v1;

import com.cic.inventory.controllers.AbstractController;
import com.cic.inventory.dtos.MaintenanceRequestDTO;
import com.cic.inventory.dtos.MaintenanceResponseDTO;
import com.cic.inventory.entities.MaintenanceStatus;
import com.cic.inventory.services.MaintenanceService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/v1/maintenances")
@RequiredArgsConstructor
public class MaintenanceController extends AbstractController {
    private final MaintenanceService maintenanceService;
    private final ModelMapper modelMapper;

    @GetMapping
    public Page<MaintenanceResponseDTO> getAll(Pageable pageable) {
        return maintenanceService.getAllMaintenance(pageable);
    }

    @GetMapping("/{id}")
    public MaintenanceResponseDTO getById(@PathVariable Long id) {
        return maintenanceService.getMaintenanceById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MaintenanceResponseDTO create(@RequestBody MaintenanceRequestDTO dto) {
        return maintenanceService.createMaintenance(dto);
    }

    @PutMapping("/{id}")
    public MaintenanceResponseDTO update(@PathVariable Long id,
                                         @RequestBody MaintenanceRequestDTO dto) {
        return maintenanceService.updateMaintenanceById(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        maintenanceService.deleteMaintenance(id);
    }

    @GetMapping("/asset/{assetId}")
    public List<MaintenanceResponseDTO> getByAsset(@PathVariable String assetId) {
        return maintenanceService.getMaintenanceByAssetId(assetId);
    }

    @GetMapping("/status/{status}")
    public List<MaintenanceResponseDTO> getByStatus(@PathVariable MaintenanceStatus status) {
        return maintenanceService.getMaintenanceByStatus(status);
    }
}
