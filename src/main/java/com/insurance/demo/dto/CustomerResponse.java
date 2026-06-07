package com.insurance.demo.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerResponse {

    private Long customerId;

    private String customerName;

    private String city;

    private String nomineeName;
}