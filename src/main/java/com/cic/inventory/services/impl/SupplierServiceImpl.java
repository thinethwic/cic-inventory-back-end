package com.cic.inventory.services.impl;

import com.cic.inventory.entities.Location;
import com.cic.inventory.entities.Supplier;
import com.cic.inventory.exceptions.InventoryException;
import com.cic.inventory.repositories.SupplierRepositories;
import com.cic.inventory.services.SupplierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {
    private final ModelMapper modelMapper;
    private final SupplierRepositories supplierRepositories;

    @Override
    public Supplier createNewSupplier(Supplier supplier) {
        try {
            return supplierRepositories.save(supplier);
        } catch (Exception exception) {
            log.error("Failed to create new supplier", exception);
            throw new InventoryException("Failed to create new supplier", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Page<Supplier> getAllSuppliers(Pageable pageable) {
        try {
            return supplierRepositories.findAll(pageable);
        } catch (Exception exception) {
            log.error("Failed to get all locations", exception);
            throw new InventoryException("Failed to get all locations", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Supplier getSupplierById(Long id) {
        try {
            return supplierRepositories.findById(id).orElseThrow(
                    () -> new InventoryException("Supplier Not Found", HttpStatus.NOT_FOUND)
            );
        }catch (InventoryException inventoryException){
            log.warn("Supplier not found with id: {} to fetch", id, inventoryException);
            throw new InventoryException("Supplier Not found", HttpStatus.NOT_FOUND);
        }
        catch (Exception exception) {
            log.error("Supplier not found with id: {} to fetch", id, exception);
            throw new InventoryException("Failed to find supplier by id", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Supplier updateSupplierById(Long id, Supplier updatedSupplier) {
        try {
            Supplier supplier =  supplierRepositories.findById(id).orElseThrow(
                    () -> new InventoryException("Location Not Found", HttpStatus.NOT_FOUND)
            );
            modelMapper.map(updatedSupplier, supplier);
            return supplierRepositories.save(supplier);

        }catch (InventoryException inventoryException){
            log.warn("Supplier not found with id: {} to fetch", id, inventoryException);
            throw new InventoryException("Supplier Not found", HttpStatus.NOT_FOUND);
        }
        catch (Exception exception) {
            log.error("Supplier not found with id: {} to fetch", id, exception);
            throw new InventoryException("Failed to find supplier by id", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void deleteSupplier(Long id) {
        try {
            supplierRepositories.deleteById(id);
        } catch (Exception exception) {
            log.error("Failed to delete supplier with id {}", id, exception);
            throw new InventoryException("Failed to delete supplier", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
