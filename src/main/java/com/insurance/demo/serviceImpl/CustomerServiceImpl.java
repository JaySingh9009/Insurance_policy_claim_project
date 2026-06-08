package com.insurance.demo.serviceImpl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.insurance.demo.dto.CustomerRequest;
import com.insurance.demo.dto.CustomerResponse;
import com.insurance.demo.entity.Customer;
import com.insurance.demo.entity.User;
import com.insurance.demo.exception.ResourceNotFoundException;
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
    	        new ResourceNotFoundException(
    	            "Customer Not Found"));

        return new CustomerResponse(
                customer.getCustomerId(),
                customer.getUser()
                        .getFullName(),
                customer.getCity(),
                customer.getNomineeName()
        );
    }
    
    @Override
    public CustomerResponse updateCustomer(
            Long customerId,
            CustomerRequest request) {

        Customer customer =
                customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Customer not found"));

        customer.setAddress(request.getAddress());
        customer.setCity(request.getCity());
        customer.setState(request.getState());
        customer.setPincode(request.getPincode());
        customer.setNomineeName(
                request.getNomineeName());
        customer.setNomineeRelation(
                request.getNomineeRelation());

        customerRepository.save(customer);

        return new CustomerResponse(
                customer.getCustomerId(),
                customer.getUser().getFullName(),
                customer.getCity(),
                customer.getNomineeName());
    }
    @Override
    public List<CustomerResponse>
    getAllCustomers() {

        return customerRepository.findAll()
                .stream()
                .map(customer ->
                        new CustomerResponse(
                                customer.getCustomerId(),
                                customer.getUser()
                                        .getFullName(),
                                customer.getCity(),
                                customer.getNomineeName()))
                .toList();
    }
    
    @Override
    public CustomerResponse getMyProfile(
            String email) {

        User user =
                userRepository.findByEmail(email)
                .orElseThrow();

        Customer customer =
                customerRepository
                .findByUser_Id(user.getId())
                .orElseThrow();

        return new CustomerResponse(
                customer.getCustomerId(),
                user.getFullName(),
                customer.getCity(),
                customer.getNomineeName());
    }
    
}