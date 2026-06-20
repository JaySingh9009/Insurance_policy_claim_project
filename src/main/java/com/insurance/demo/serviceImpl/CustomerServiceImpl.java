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
public class CustomerServiceImpl implements CustomerService {

	private final CustomerRepository customerRepository;

	private final UserRepository userRepository;

//	@Override
//	public CustomerResponse createCustomer(CustomerRequest request) {
//
//		User user = userRepository.findById(request.getUserId())
//				.orElseThrow(() -> new RuntimeException("User Not Found"));
//
//		Customer customer = Customer.builder().address(request.getAddress()).city(request.getCity())
//				.state(request.getState()).pincode(request.getPincode()).nomineeName(request.getNomineeName())
//				.nomineeRelation(request.getNomineeRelation()).user(user).build();
//
//		customerRepository.save(customer);
//
//		return new CustomerResponse(customer.getCustomerId(), user.getFullName(), customer.getCity(),
//				customer.getNomineeName());
//	}

	@Override
	public CustomerResponse createProfile(String email, CustomerRequest request) {

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User Not Found"));

		// Ek user ka sirf ek hi customer profile ho
		if (customerRepository.findByUser_Id(user.getId()).isPresent()) {

			throw new RuntimeException("Customer profile already exists");
		}

		Customer customer = Customer.builder().address(request.getAddress()).city(request.getCity())
				.state(request.getState()).pincode(request.getPincode()).nomineeName(request.getNomineeName())
				.nomineeRelation(request.getNomineeRelation()).user(user).build();

		customer = customerRepository.save(customer);

		return mapToResponse(customer);
	}

//	@Override
//	public CustomerResponse getCustomer(Long userId) {
//
//		Customer customer = customerRepository.findByUser_Id(userId)
//				.orElseThrow(() -> new ResourceNotFoundException("Customer Not Found"));
//
//		return mapToResponse(customer);
//	}

	@Override
	public CustomerResponse getCustomerProfile(String email) {

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User Not Found"));

		Customer customer = customerRepository.findByUser_Id(user.getId())
				.orElseThrow(() -> new ResourceNotFoundException("Customer Profile Not Found"));

		return mapToResponse(customer);
	}

	@Override
	public List<CustomerResponse> getAllCustomers() {

		return customerRepository.findAll().stream().map(this::mapToResponse).toList();
	}

	@Override
	public CustomerResponse updateProfile(String email, CustomerRequest request) {

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		Customer customer = customerRepository.findByUser_Id(user.getId())
				.orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

		customer.setAddress(request.getAddress());
		customer.setCity(request.getCity());
		customer.setState(request.getState());
		customer.setPincode(request.getPincode());
		customer.setNomineeName(request.getNomineeName());
		customer.setNomineeRelation(request.getNomineeRelation());

		customerRepository.save(customer);

		return mapToResponse(customer);
	}

	private CustomerResponse mapToResponse(Customer customer) {

		User user = customer.getUser();

		return CustomerResponse.builder().customerId(customer.getCustomerId()).customerName(user.getFullName())
				.address(customer.getAddress()).city(customer.getCity()).state(customer.getState())
				.pincode(customer.getPincode()).nomineeName(customer.getNomineeName())
				.nomineeRelation(customer.getNomineeRelation()).createdAt(customer.getCreatedAt())
				.updatedAt(customer.getUpdatedAt()).build();
	}
}