package com.insurance.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    private String productName;

    @NotNull(message = "Product type is required")
    private String productType;

    @NotBlank(message = "Description is required")
    private String description;
}