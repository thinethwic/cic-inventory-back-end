package com.cic.inventory.dtos.responses;

import com.cic.inventory.entities.types.AssetStatus;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

@Data
public class AssetResponseDTO {
    private Long id;
    private String assetCode;
    private String barcode;
    private String category;
    private String brand;
    private String model;
    private String serialNo;
    private AssetStatus status;

    private String location;
    private Long locationId;      // ← ADD THIS

    private String assignedTo;
    private Long assignedToId;    // ← ADD THIS

    private Long supplierId;
    private String supplierName;

    private LocalDate purchaseDate;
    private LocalDate warrantyEnd;
    private String qrCode;

    private Date createdAt;
    private Date updatedAt;
}
