package com.insurance.demo.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.insurance.demo.dto.ProductRequest;
import com.insurance.demo.dto.ProductResponse;

public interface ProductService {

	ProductResponse createProduct(ProductRequest request);

	ProductResponse getProductById(Long id);

	ProductResponse updateProduct(Long id, ProductRequest request);
	
	void deactivateProduct(Long id);

	List<ProductResponse> getAllProducts();

	Page<ProductResponse> getProducts(int page, int size, String sortBy);
}