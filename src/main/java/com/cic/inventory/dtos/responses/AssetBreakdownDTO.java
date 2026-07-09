package com.cic.inventory.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetBreakdownDTO {
    private Map<String, Long> statusCounts;
    private Map<String, Long> categoryCounts;
}
