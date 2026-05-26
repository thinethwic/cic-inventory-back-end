package com.cic.inventory.repositories;

import com.cic.inventory.entities.UserSession;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {
    @EntityGraph(attributePaths = "user")
    Optional<UserSession> findByIdAndRevokedAtIsNull(UUID id);

    @Override
    @EntityGraph(attributePaths = "user")
    Optional<UserSession> findById(UUID uuid);

    void deleteByExpiresAtBefore(Instant instant);
}
