package com.cic.inventory.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DepartmentDTO {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Department Code is required")
    @Size(min = 6, max = 50, message = "Department Code must be between 6 and 50 characters")
    private String code;
}
