package com.cic.inventory.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserPrincipal {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String location;
    private String departmentName;
}
