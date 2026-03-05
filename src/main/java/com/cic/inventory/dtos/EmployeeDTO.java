package com.cic.inventory.dtos;

import com.cic.inventory.entities.Department;
import com.cic.inventory.entities.EmployeeStatus;
import com.cic.inventory.entities.Location;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EmployeeDTO {

    @NotNull(message = "Department ID cannot be null")
    private Department department;

    @NotNull(message = "Location ID cannot be null")
    private Location location;

    private String name;

    @Size(max = 10, message = "Phone number must be 10 characters")
    private String phone_no;

    @Email(message = "Email must be valid")
    private String email;

    @NotNull(message = "Employee status cannot be null")
    private EmployeeStatus employeeStatus;
}
