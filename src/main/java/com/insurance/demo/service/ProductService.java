package com.insurance.demo.service;

import java.util.List;

import com.insurance.demo.dto.ProductRequest;
import com.insurance.demo.dto.ProductResponse;

public interface ProductService {

	ProductResponse createProduct(ProductRequest request);

	List<ProductResponse> getAllProducts();
}