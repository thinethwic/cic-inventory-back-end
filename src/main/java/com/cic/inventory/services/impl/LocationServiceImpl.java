package com.cic.inventory.services.impl;

import com.cic.inventory.entities.Location;
import com.cic.inventory.exceptions.InventoryException;
import com.cic.inventory.repositories.LocationRepositories;
import com.cic.inventory.services.LocationService;
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
public class LocationServiceImpl implements LocationService {
    private final ModelMapper modelMapper;
    private final LocationRepositories locationRepositories;

    @Override
    public Location createNewLocation(Location location) {
        try {
            return locationRepositories.save(location);
        } catch (DataIntegrityViolationException e){
            log.error("Data integrity violation while creating Location: {}", e.getMessage());
            throw new InventoryException("Location with this code already exists", HttpStatus.CONFLICT);
        } catch (Exception exception) {
            log.error("Failed to create new location", exception);
            throw new InventoryException("Failed to create new location", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Page<Location> getAllLocations(Pageable pageable) {
        try {
            return locationRepositories.findAll(pageable);
        } catch (Exception exception) {
            log.error("Failed to get all locations", exception);
            throw new InventoryException("Failed to get all locations", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Location getLocationById(Long id) {
        try {
            Location location =  locationRepositories.findById(id).orElseThrow(
                    () -> new InventoryException("Location Not Found", HttpStatus.NOT_FOUND)
            );
            return location;
        }catch (InventoryException inventoryException){
            log.warn("Location not found with id: {} to fetch", id, inventoryException);
            throw new InventoryException("Location Not found", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public Location updateLocationById(Long id, Location updatedLocation) {
        try {
            Location location =  locationRepositories.findById(id).orElseThrow(
                    () -> new InventoryException("Location Not Found", HttpStatus.NOT_FOUND)
            );
            modelMapper.map(updatedLocation, location);
            return locationRepositories.save(location);
        }catch (InventoryException inventoryException){
            log.warn("Location not found with id: {} to fetch", id, inventoryException);
            throw new InventoryException("Location Not found", HttpStatus.NOT_FOUND);

        } catch (Exception exception) {
            log.error("Error updating location", exception);
            throw new InventoryException("Failed to update location", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void deleteLocation(Long id) {
        try {
            locationRepositories.deleteById(id);
        } catch (Exception exception) {
            log.error("Failed to delete department with id {}", id, exception);
            throw new InventoryException("Failed to delete department", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
