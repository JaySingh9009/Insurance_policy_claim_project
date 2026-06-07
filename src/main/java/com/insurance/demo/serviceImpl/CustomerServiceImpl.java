package com.insurance.demo.serviceImpl;

import org.springframework.stereotype.Service;

import com.insurance.demo.dto.CustomerRequest;
import com.insurance.demo.dto.CustomerResponse;
import com.insurance.demo.entity.Customer;
import com.insurance.demo.entity.User;
import com.insurance.demo.repository.CustomerRepository;
import com.insurance.demo.repository.UserRepository;
import com.insurance.demo.service.CustomerService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl
        implements CustomerService {

    private final CustomerRepository customerRepository;

    private final UserRepository userRepository;

    @Override
    public CustomerResponse createCustomer(
            CustomerRequest request) {

        User user =
                userRepository.findById(
                        request.getUserId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User Not Found"));

        Customer customer =
                Customer.builder()
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .nomineeName(
                        request.getNomineeName())
                .nomineeRelation(
                        request.getNomineeRelation())
                .user(user)
                .build();

        customerRepository.save(customer);

        return new CustomerResponse(
                customer.getCustomerId(),
                user.getFullName(),
                customer.getCity(),
                customer.getNomineeName()
        );
    }

    @Override
    public CustomerResponse getCustomer(
            Long userId) {

    	Customer customer =
    	        customerRepository
    	        .findByUser_Id(userId)
    	        .orElseThrow(() ->
    	                new RuntimeException(
    	                        "Customer Not Found"));

        return new CustomerResponse(
                customer.getCustomerId(),
                customer.getUser()
                        .getFullName(),
                customer.getCity(),
                customer.getNomineeName()
        );
    }
}