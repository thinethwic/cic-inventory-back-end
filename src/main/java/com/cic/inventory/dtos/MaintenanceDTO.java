package com.cic.inventory.dtos;

import com.cic.inventory.entities.types.MaintenancePriority;
import com.cic.inventory.entities.types.MaintenanceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceDTO {

    @NotBlank(message = "Ticket number is required")
    private String ticketNo;

    @NotNull(message = "Asset is required")
    private Long assetId;

    private String issueTitle;
    private String description;

    @NotNull(message = "Priority is required")
    private MaintenancePriority priority;

    @NotNull(message = "Status is required")
    private MaintenanceStatus status;

    @NotNull(message = "Reported date is required")
    private LocalDate reportedDate;

    private LocalDate dueDate;
    private String assignedTo;
    private BigDecimal cost;
    private String notes;
}