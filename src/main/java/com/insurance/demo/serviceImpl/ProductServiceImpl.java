package com.insurance.demo.serviceImpl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.insurance.demo.dto.ProductRequest;
import com.insurance.demo.dto.ProductResponse;
import com.insurance.demo.entity.InsuranceProduct;
import com.insurance.demo.exception.ResourceNotFoundException;
import com.insurance.demo.repository.ProductRepository;
import com.insurance.demo.service.ProductService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl
        implements ProductService {

    private final ProductRepository
            productRepository;

    @Override
    public ProductResponse createProduct(
            ProductRequest request) {

        if (productRepository
                .existsByProductName(
                        request.getProductName())) {

            throw new ResourceNotFoundException(
                    "Product already exists");
        }

        InsuranceProduct product =
                InsuranceProduct.builder()
                .productName(
                        request.getProductName())
                .productType(
                        request.getProductType())
                .description(
                        request.getDescription())
                .active(
                        request.isActive())
                .build();

        productRepository.save(product);

        return new ProductResponse(
                product.getProductId(),
                product.getProductName(),
                product.getProductType()
                       .name(),
                product.getDescription(),
                product.isActive());
    }

    @Override
    public List<ProductResponse>
    getAllProducts() {

        return productRepository
                .findAll()
                .stream()
                .map(product ->
                        new ProductResponse(
                                product.getProductId(),
                                product.getProductName(),
                                product.getProductType()
                                       .name(),
                                product.getDescription(),
                                product.isActive()))
                .toList();
    }

    @Override
    public Page<ProductResponse>
    getProducts(
            int page,
            int size,
            String sortBy) {

        return productRepository
                .findAll(
                        PageRequest.of(
                                page,
                                size,
                                Sort.by(sortBy)))
                .map(product ->
                        new ProductResponse(
                                product.getProductId(),
                                product.getProductName(),
                                product.getProductType()
                                       .name(),
                                product.getDescription(),
                                product.isActive()));
    }
}