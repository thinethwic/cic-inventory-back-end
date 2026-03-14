package com.cic.inventory.dtos;

import com.cic.inventory.entities.types.AssetStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AssetDTO {

    @NotBlank(message = "Asset Code is required")
    @Size(min = 2, max = 50)
    private String assetCode;

    @Size(max = 50)
    private String barcode;

    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category must not exceed 100 characters")
    @Pattern(
            regexp = "^[A-Za-z0-9 &()_./+-]+$",
            message = "Category contains invalid characters"
    )
    private String category;

    @NotBlank(message = "Brand is required")
    private String brand;

    @NotBlank(message = "Model is required")
    private String model;

    @NotBlank(message = "Serial No is required")
    @Size(min = 1, max = 100)
    private String serialNo;

    @NotNull(message = "Asset Status is required")
    private AssetStatus status;

    // Foreign keys
    @NotNull(message = "Location is required")
    private Long locationId;

    private Long assignedToId; // optional employee

    @NotNull(message = "Supplier is required")
    private Long supplierId;

    @NotNull(message = "Purchase date is required")
    private LocalDate purchaseDate;

    @NotNull(message = "Warranty end date is required")
    private LocalDate warrantyEnd;

    private String qrCode; // better as string if using QR values
}