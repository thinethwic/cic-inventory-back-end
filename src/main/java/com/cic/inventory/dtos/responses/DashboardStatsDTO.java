package com.cic.inventory.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    private long total;
    private long assigned;
    private long inRepair;
    private long disposed;
    private List<WarrantyExpiringDTO> warrantyExpiring;
}
