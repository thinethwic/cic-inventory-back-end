package com.cic.inventory.controllers.v1;

import com.cic.inventory.controllers.AbstractController;
import com.cic.inventory.dtos.MaintenanceDTO;
import com.cic.inventory.entities.Maintenance;
import com.cic.inventory.services.MaintenanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/maintenances")
@RequiredArgsConstructor
public class MaintenanceController extends AbstractController {

    private final MaintenanceService maintenanceService;

    @GetMapping
    public ResponseEntity<Page<Maintenance>> getAllMaintenance(Pageable pageable) {
        return sendOkResponse(maintenanceService.getAllMaintenance(pageable));
    }

    @GetMapping("{id}")
    public ResponseEntity<Maintenance> getMaintenanceById(@PathVariable Long id) {
        return sendOkResponse(maintenanceService.getMaintenanceById(id));
    }

    @PostMapping
    public ResponseEntity<Maintenance> createMaintenance(@Validated @RequestBody MaintenanceDTO maintenanceDTO) {
        return sendCreatedResponse(maintenanceService.createNewMaintenance(maintenanceDTO));
    }

    @PutMapping("{id}")
    public ResponseEntity<Maintenance> updateMaintenance(
            @PathVariable Long id,
            @Valid @RequestBody MaintenanceDTO maintenanceDTO) {
        return sendOkResponse(maintenanceService.updateMaintenanceById(id, maintenanceDTO));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteMaintenance(@PathVariable Long id) {
        maintenanceService.deleteMaintenance(id);
        return sendNoContentResponse();
    }
}