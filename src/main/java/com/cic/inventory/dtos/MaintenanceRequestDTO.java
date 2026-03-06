package com.cic.inventory.dtos;

import com.cic.inventory.entities.MaintenancePriority;
import com.cic.inventory.entities.MaintenanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceRequestDTO {
    private String ticketNo;
    private String assetId;
    private String assetCode;
    private String issueTitle;
    private String description;
    private MaintenancePriority priority;
    private MaintenanceStatus status;
    private LocalDate reportedDate;
    private LocalDate dueDate;
    private LocalDate completedDate;
    private String assignedTo;
    private String supplier;
    private BigDecimal cost;
    private String notes;
}
