package com.cic.inventory.configs;

import com.cic.inventory.dtos.MaintenanceResponse;
import com.cic.inventory.dtos.UserSummaryDTO;
import com.cic.inventory.entities.Maintenance;

public class MaintenanceMapper {

    public static MaintenanceResponse toResponse(Maintenance m) {
        UserSummaryDTO createdBy = m.getCreatedBy() != null
                ? new UserSummaryDTO(
                m.getCreatedBy().getId(),
                m.getCreatedBy().getFirstName(),
                m.getCreatedBy().getLastName())
                : null;

        UserSummaryDTO updatedBy = m.getUpdatedBy() != null
                ? new UserSummaryDTO(
                m.getUpdatedBy().getId(),
                m.getUpdatedBy().getFirstName(),
                m.getUpdatedBy().getLastName())
                : null;

        return MaintenanceResponse.builder()
                .id(m.getId())
                .ticketNo(m.getTicketNo())
                .assetId(m.getAsset().getId())
                .assetCode(m.getAsset().getAssetCode())
                .issueTitle(m.getIssueTitle())
                .description(m.getDescription())
                .priority(m.getPriority())
                .status(m.getStatus())
                .reportedDate(m.getReportedDate())
                .dueDate(m.getDueDate())
                .assignedTo(m.getAssignedTo())
                .cost(m.getCost())
                .location(m.getLocation())
                .notes(m.getNotes())
                .createdBy(createdBy)
                .updatedBy(updatedBy)
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .build();
    }
}
