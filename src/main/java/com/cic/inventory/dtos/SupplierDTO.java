package com.cic.inventory.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SupplierDTO {

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Phone no is required")
    private String phone_no;

    @Email(message = "Email must be valid")
    private String email;
}
