package com.insurance.demo.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerResponse {

	private Long customerId;

	private String customerName;

	private String address;

	private String city;

	private String state;

	private String pincode;

	private String nomineeName;

	private String nomineeRelation;

	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}