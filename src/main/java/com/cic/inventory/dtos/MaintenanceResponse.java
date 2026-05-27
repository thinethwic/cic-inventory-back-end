package com.cic.inventory.dtos;

import com.cic.inventory.entities.types.MaintenancePriority;
import com.cic.inventory.entities.types.MaintenanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceResponse {
    private Long id;
    private String ticketNo;
    private Long assetId;
    private String assetCode;
    private String issueTitle;
    private String description;
    private MaintenancePriority priority;
    private MaintenanceStatus status;
    private LocalDate reportedDate;
    private LocalDate dueDate;
    private String assignedTo;
    private BigDecimal cost;
    private String location;
    private String notes;

    // ── Audit — typed DTOs, not raw User entities ─────────────────────────
    private UserSummaryDTO createdBy;
    private UserSummaryDTO updatedBy;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
