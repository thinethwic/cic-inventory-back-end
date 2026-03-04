package com.cic.inventory.services;

import com.cic.inventory.entities.Department;
import com.cic.inventory.entities.Location;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LocationService {
    Location createNewLocation(Location  location);
    Page<Location> getAllLocations(Pageable pageable);
    Location getLocationById(Long id);
    Location updateLocationById(Long id, Location updatedLocation);
    void deleteLocation(Long id);
}
