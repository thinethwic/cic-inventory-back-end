package com.cic.inventory.controllers.v1;

import com.cic.inventory.controllers.AbstractController;
import com.cic.inventory.dtos.SupplierDTO;
import com.cic.inventory.entities.Supplier;
import com.cic.inventory.services.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/suppliers")
@RequiredArgsConstructor
public class SupplierController extends AbstractController {
    private final ModelMapper modelMapper;
    private final SupplierService supplierService;

    @GetMapping
    public ResponseEntity<Page<Supplier>> getAllSuppliers(Pageable pageable) {
        Page<Supplier> suppliers = supplierService.getAllSuppliers(pageable);
        return sendOkResponse(suppliers);
    }

    @GetMapping("{id}")
    public ResponseEntity<Supplier> getSupplierById(@PathVariable Long id) {
        Supplier supplier = supplierService.getSupplierById(id);
        return sendOkResponse(supplier);
    }

    @PostMapping
    public ResponseEntity<Supplier> createSupplier(@Validated @RequestBody SupplierDTO supplierDTO) {
        Supplier supplier = modelMapper.map(supplierDTO,Supplier.class);
        Supplier createSupplier = supplierService.createNewSupplier(supplier);
        return sendCreatedResponse(createSupplier);
    }

    @PutMapping("{id}")
    public ResponseEntity<Supplier> updateSupplier(@PathVariable Long id, @Valid @RequestBody SupplierDTO updateSupplierDTO) {
        Supplier supplier = modelMapper.map(updateSupplierDTO, Supplier.class);
        Supplier updatedSupplierById = supplierService.updateSupplierById(id, supplier);
        return sendOkResponse(updatedSupplierById);

    }

    @DeleteMapping("{id}")
    public ResponseEntity<Supplier> deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return sendNoContentResponse();
    }
}
