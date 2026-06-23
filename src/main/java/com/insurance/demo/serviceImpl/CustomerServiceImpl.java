package com.insurance.demo.serviceImpl;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.insurance.demo.dto.CustomerRequest;
import com.insurance.demo.dto.CustomerResponse;
import com.insurance.demo.dto.PagedResponse;
import com.insurance.demo.entity.Customer;
import com.insurance.demo.entity.User;
import com.insurance.demo.exception.BadRequestException;
import com.insurance.demo.exception.ResourceNotFoundException;
import com.insurance.demo.repository.CustomerRepository;
import com.insurance.demo.repository.UserRepository;
import com.insurance.demo.service.CustomerService;
import com.insurance.demo.util.PaginationValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("createdAt", "city", "state");

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    @Override
    public CustomerResponse createProfile(CustomerRequest request, Long userId) {
        log.info("Creating customer profile for userId={}", userId);

        User user = findUser(userId);

        // One user → one profile only
        if (customerRepository.findByUser_Id(userId).isPresent()) {
            log.warn("Customer profile already exists for userId={}", userId);
            throw new BadRequestException("Customer profile already exists for this account");
        }
        
  

     if (request.getDateOfBirth() != null) {
         int age = Period.between(request.getDateOfBirth(), LocalDate.now()).getYears();
         if (age < 18) {
             throw new BadRequestException("Customer must be at least 18 years old to register");
         }
         if (age > 100) {
             throw new BadRequestException("Please enter a valid date of birth");
         }
     }

        Customer customer = Customer.builder()
                .user(user)
                .dateOfBirth(request.getDateOfBirth())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .nomineeName(request.getNomineeName())
                .nomineeRelation(request.getNomineeRelation())
                .build();

        customer = customerRepository.save(customer);
        log.info("Customer profile created: customerId={}", customer.getCustomerId());
        return mapToResponse(customer);
    }

    @Override
    public CustomerResponse updateProfile(CustomerRequest request, Long userId) {
        log.info("Updating customer profile for userId={}", userId);
        
 

     if (request.getDateOfBirth() != null) {
         int age = Period.between(request.getDateOfBirth(), LocalDate.now()).getYears();
         if (age < 18) {
             throw new BadRequestException("Customer must be at least 18 years old to register");
         }
         if (age > 100) {
             throw new BadRequestException("Please enter a valid date of birth");
         }
     }

        Customer customer = customerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found for this account"));

        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setAddress(request.getAddress());
        customer.setCity(request.getCity());
        customer.setState(request.getState());
        customer.setPincode(request.getPincode());
        customer.setNomineeName(request.getNomineeName());
        customer.setNomineeRelation(request.getNomineeRelation());

        customer = customerRepository.save(customer);
        log.info("Customer profile updated: customerId={}", customer.getCustomerId());
        return mapToResponse(customer);
    }

    @Override
    public CustomerResponse getMyProfile(Long userId) {
        Customer customer = customerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found for this account"));
        return mapToResponse(customer);
    }

    @Override
    public PagedResponse<CustomerResponse> getAllCustomers(int page, int size, String sortBy, String sortDir) {
        PaginationValidator.validate(page, size, sortBy, ALLOWED_SORT_FIELDS);
        Sort sort = "desc".equalsIgnoreCase(sortDir)
                ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Customer> customerPage = customerRepository.findAll(pageable);
        List<CustomerResponse> records = customerPage.getContent().stream().map(this::mapToResponse).toList();

        return PagedResponse.<CustomerResponse>builder()
                .records(records)
                .currentPage(customerPage.getNumber())
                .pageSize(customerPage.getSize())
                .totalRecords(customerPage.getTotalElements())
                .totalPages(customerPage.getTotalPages())
                .isLastPage(customerPage.isLast())
                .build();
    }

    @Override
    public CustomerResponse getCustomerById(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));
        return mapToResponse(customer);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }

    private CustomerResponse mapToResponse(Customer c) {
        return CustomerResponse.builder()
                .customerId(c.getCustomerId())
                .fullName(c.getUser().getFullName())
                .email(c.getUser().getEmail())
                .mobileNumber(c.getUser().getMobileNumber())
                .dateOfBirth(c.getDateOfBirth())
                .address(c.getAddress())
                .city(c.getCity())
                .state(c.getState())
                .pincode(c.getPincode())
                .nomineeName(c.getNomineeName())
                .nomineeRelation(c.getNomineeRelation())
                .createdAt(c.getCreatedAt())
                .build();
    }
}