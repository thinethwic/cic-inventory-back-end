package com.cic.inventory.controllers.v1;

import com.cic.inventory.controllers.AbstractController;
import com.cic.inventory.dtos.DepartmentDTO;
import com.cic.inventory.dtos.LocationDTO;
import com.cic.inventory.entities.Department;
import com.cic.inventory.entities.Location;
import com.cic.inventory.services.DepartmentService;
import com.cic.inventory.services.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/locations")
@RequiredArgsConstructor
public class LocationController extends AbstractController {
    private final ModelMapper modelMapper;
    private final LocationService locationService;

    @GetMapping
    public ResponseEntity<Page<Location>> getAllLocations(Pageable pageable) {
        Page<Location> locations = locationService.getAllLocations(pageable);
        return sendOkResponse(locations);
    }

    @GetMapping("{id}")
    public ResponseEntity<Location> getLocationById(@PathVariable Long id) {
        Location location = locationService.getLocationById(id);
        return sendOkResponse(location);
    }

    @PostMapping
    public ResponseEntity<Location> createLocation(@Validated @RequestBody LocationDTO locationDTO) {
        Location location = modelMapper.map(locationDTO,Location.class);
        Location createLocation = locationService.createNewLocation(location);
        return sendCreatedResponse(createLocation);
    }

    @PutMapping("{id}")
    public ResponseEntity<Location> updateLocation(@PathVariable Long id, @Valid @RequestBody LocationDTO UpdatelocationDTO) {
        Location location = modelMapper.map(UpdatelocationDTO, Location.class);
        Location updatedLocation = locationService.updateLocationById(id, location);
        return sendOkResponse(updatedLocation);

    }

    @DeleteMapping("{id}")
    public ResponseEntity<Location> deleteLocation(@PathVariable Long id) {
        locationService.deleteLocation(id);
        return sendNoContentResponse();
    }
}
