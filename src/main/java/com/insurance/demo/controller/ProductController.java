package com.insurance.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.insurance.demo.dto.ProductRequest;
import com.insurance.demo.dto.ProductResponse;
import com.insurance.demo.service.ProductService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

	private final ProductService productService;

	@PostMapping
	public ResponseEntity<ProductResponse> createProduct(@RequestBody ProductRequest request) {

		return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(request));
	}

	@GetMapping
	public ResponseEntity<List<ProductResponse>> getAllProducts() {

		return ResponseEntity.ok(productService.getAllProducts());
	}
}