package com.cic.inventory.dtos;

import com.cic.inventory.entities.AssetCategory;
import com.cic.inventory.entities.AssetStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AssetDTO {

    @NotBlank(message = "Asset Code is required")
    @Size(min = 2, max = 50, message = "Asset Code must be between 2 and 50 characters")
    private String assetCode;

    @NotBlank(message = "Barcode is required")
    @Size(min = 1, max = 50, message = "Barcode must be between 1 and 50 characters")
    private String barcode;

    @NotNull(message = "Asset Category is required")
    private AssetCategory category;

    @NotBlank(message = "Brand is required")
    private String brand;

    @NotBlank(message = "Model is required")
    private String model;

    @NotBlank(message = "Serial No is required")
    @Size(min = 1, max = 100, message = "Serial No must be between 1 and 100 characters")
    private String serialNo;

    @NotNull(message = "Asset Status is required")
    private AssetStatus status;

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "Assign Person is required")
    private String assignedTo;

    @NotBlank(message = "Purchasing date is required")
    private LocalDate purchaseDate;

    @NotBlank(message = "Warranty date is required")
    private LocalDate warrantyEnd;
}
