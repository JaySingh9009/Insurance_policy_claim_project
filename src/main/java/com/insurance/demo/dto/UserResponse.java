package com.insurance.demo.dto;

import com.insurance.demo.enums.Role;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponse {

    private Long id;

    private String fullName;

    private String email;

    private String mobileNumber;

    private Role role;

    private boolean active;
}