package com.cic.inventory.entities;

import com.cic.inventory.entities.types.AssetStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "assets")
@Data
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "asset_code", nullable = false, unique = true)
    private String assetCode;

    @Column(name = "barcode", unique = true)
    private String barcode;

    @Column(name = "category", nullable = false, length = 100)
    private String category;

    @Column(name = "brand")
    private String brand;

    @Column(name = "model")
    private String model;

    @Column(name = "serial_no", unique = true)
    private String serialNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AssetStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_id", nullable = true)
    private Employee assignedTo;

    // ── Supplier relationship ─────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "warranty_end")
    private LocalDate warrantyEnd;

    @Column(name = "qrCode")
    private String qrCode;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;

    // Excluded from equals/hashCode/toString (Lombok @Data would otherwise
    // touch this lazy collection outside a transaction) and from JSON
    // (attachments are only ever exposed through the dedicated
    // /assets/{id}/attachments endpoints, as AssetAttachmentDTO — never the
    // raw entity, which would leak the on-disk file path).
    @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private List<AssetAttachment> attachments = new ArrayList<>();
}