package com.insurance.demo.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerResponse {

    private Long customerId;
    private String fullName;
    private String email;
    private String mobileNumber;
    private LocalDate dateOfBirth;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String nomineeName;
    private String nomineeRelation;
    private LocalDateTime createdAt;
}