package com.insurance.demo.dto;

import lombok.Data;

@Data
public class CreateAgentRequest {

    private String fullName;

    private String email;

    private String password;

    private String mobileNumber;
}