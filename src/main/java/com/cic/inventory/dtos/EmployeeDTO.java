package com.cic.inventory.dtos;

import com.cic.inventory.entities.Department;
import com.cic.inventory.entities.types.EmployeeStatus;
import com.cic.inventory.entities.Location;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EmployeeDTO {

    private Long id;

    @NotNull(message = "Employee ID cannot be null")
    private String empId;

    @NotNull
    private DepartmentRef department;

    @NotNull
    private LocationRef location;

    private String name;

    @Size(max = 10)
    private String phone_no;

    @Email
    private String email;

    @NotNull
    private EmployeeStatus employeeStatus;

    @Data
    public static class DepartmentRef {
        private Long id;
        private String name;
        private String code;
    }

    @Data
    public static class LocationRef {
        private Long id;
        private String name;
        private String code;
    }
}
