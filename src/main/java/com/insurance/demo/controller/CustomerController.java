package com.insurance.demo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.insurance.demo.dto.CustomerRequest;
import com.insurance.demo.dto.CustomerResponse;
import com.insurance.demo.service.CustomerService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(
            @RequestBody CustomerRequest request) {

        return ResponseEntity.ok(
                customerService.createCustomer(request));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<CustomerResponse> getCustomer(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                customerService.getCustomer(userId));
    }

    @PutMapping("/{customerId}")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable Long customerId,
            @RequestBody CustomerRequest request) {

        return ResponseEntity.ok(
                customerService.updateCustomer(
                        customerId,
                        request));
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponse>>
    getAllCustomers() {

        return ResponseEntity.ok(
                customerService.getAllCustomers());
    }
    
    @GetMapping("/me")
    public ResponseEntity<CustomerResponse>
    getMyProfile(Authentication authentication) {

        String email =
                authentication.getName();

        return ResponseEntity.ok(
                customerService
                .getMyProfile(email));
    }
}