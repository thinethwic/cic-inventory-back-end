package com.cic.inventory.dtos;

import com.cic.inventory.entities.types.AssetStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
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

    @PositiveOrZero(message = "Asset value must not be negative")
    @Digits(integer = 13, fraction = 2, message = "Asset value must have at most 13 digits and 2 decimal places")
    private BigDecimal assetValue; // optional

    private LocalDate purchaseDate;

    private LocalDate warrantyEnd;

    private String qrCode; // better as string if using QR values
}