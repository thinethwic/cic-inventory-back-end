package com.cic.inventory.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.util.Date;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "assetTransfers")
@Data
public class AssetTransfer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transferType")
    private String TransferType;

    @Column(name = "transferDate")
    private LocalDate TransferDate;

    @Column(name = "reason")
    private String reason;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    // ── From state (snapshot before transfer) ────────────────────────────────
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "from_employee_id", nullable = true)
    private Employee fromEmployee;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "to_employee_id", nullable = true)
    private Employee toEmployee;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "from_location_id", nullable = true)
    private Location fromLocation;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "to_location_id", nullable = true)
    private Location toLocation;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;

}
