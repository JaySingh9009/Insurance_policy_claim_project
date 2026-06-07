package com.insurance.demo.service;
import java.util.List;

import org.springframework.data.domain.Page;

import com.insurance.demo.dto.ProductRequest;
import com.insurance.demo.dto.ProductResponse;

public interface ProductService {

    ProductResponse createProduct(
            ProductRequest request);

    List<ProductResponse>
    getAllProducts();

    Page<ProductResponse>
    getProducts(
            int page,
            int size,
            String sortBy);
}