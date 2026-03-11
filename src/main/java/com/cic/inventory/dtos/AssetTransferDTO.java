package com.cic.inventory.dtos;

import com.cic.inventory.entities.Asset;
import com.cic.inventory.entities.Location;
import com.cic.inventory.entities.types.EmployeeStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AssetTransferDTO {

    @NotNull(message = "Transfer Type cannot be null")
    private String TransferType;

    @NotNull(message = "TransferDate cannot be null")
    private LocalDate TransferDate;

    @NotNull(message = "Reason cannot be null")
    private String reason;

    @NotNull(message = "Asset ID cannot be null")
    private Asset assetId;

}
