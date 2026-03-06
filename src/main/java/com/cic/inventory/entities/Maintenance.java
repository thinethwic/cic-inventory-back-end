package com.cic.inventory.entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "maintenance")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Maintenance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_no", nullable = false, unique = true, length = 20)
    private String ticketNo;               // e.g. MT-0001

    // FK to Asset — store just the ID as a string (matches your frontend assetId)
    @Column(name = "asset_id", nullable = false)
    private String assetId;

    @Column(name = "asset_code", nullable = false, length = 50)
    private String assetCode;              // snapshot for display

    @Column(name = "issue_title", nullable = false, length = 255)
    private String issueTitle;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MaintenancePriority priority;  // LOW | MEDIUM | HIGH | CRITICAL

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MaintenanceStatus status;      // OPEN | IN_PROGRESS | COMPLETED | CANCELLED

    @Column(name = "reported_date", nullable = false)
    private LocalDate reportedDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "completed_date")
    private LocalDate completedDate;

    @Column(name = "assigned_to", length = 100)
    private String assignedTo;             // technician name

    @Column(length = 100)
    private String supplier;               // vendor name

    @Column(precision = 10, scale = 2)
    private BigDecimal cost;               // LKR value

    @Column(columnDefinition = "TEXT")
    private String notes;
}
