package com.cic.inventory.repositories;

import com.cic.inventory.entities.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepositories extends JpaRepository<Location,Long> {
}
