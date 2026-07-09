package com.cic.inventory.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WarrantyExpiringDTO {
    private String assetCode;
    private String brand;
    private String model;
    private LocalDate warrantyEnd;
}
