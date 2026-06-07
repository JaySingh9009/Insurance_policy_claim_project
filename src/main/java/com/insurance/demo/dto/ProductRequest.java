package com.insurance.demo.dto;

import com.insurance.demo.enums.ProductType;

import lombok.Data;

@Data
public class ProductRequest {

	private String productName;

	private ProductType productType;

	private String description;

	private boolean active;
}