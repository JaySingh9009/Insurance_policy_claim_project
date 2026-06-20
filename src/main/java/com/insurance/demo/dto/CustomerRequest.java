package com.insurance.demo.dto;

import lombok.Data;

@Data
public class CustomerRequest {

    private String address;

    private String city;

    private String state;

    private String pincode;

    private String nomineeName;

    private String nomineeRelation;

//    private Long userId;
}