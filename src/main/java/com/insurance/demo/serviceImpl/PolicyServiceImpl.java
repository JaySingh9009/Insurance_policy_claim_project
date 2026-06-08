package com.insurance.demo.serviceImpl;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.insurance.demo.dto.PolicyResponse;
import com.insurance.demo.dto.PurchasePolicyRequest;
import com.insurance.demo.entity.Customer;
import com.insurance.demo.entity.Policy;
import com.insurance.demo.entity.PolicyPlan;
import com.insurance.demo.enums.PolicyStatus;
import com.insurance.demo.exception.ResourceNotFoundException;
import com.insurance.demo.repository.CustomerRepository;
import com.insurance.demo.repository.PolicyPlanRepository;
import com.insurance.demo.repository.PolicyRepository;
import com.insurance.demo.service.PolicyService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PolicyServiceImpl implements PolicyService {

    private final PolicyRepository policyRepository;

    private final CustomerRepository customerRepository;

    private final PolicyPlanRepository planRepository;

    @Override
    public PolicyResponse purchasePolicy(
            PurchasePolicyRequest request) {

        Customer customer =
                customerRepository.findById(
                        request.getCustomerId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Customer not found"));

        PolicyPlan plan =
                planRepository.findById(
                        request.getPlanId())
                .orElseThrow(() ->
                new ResourceNotFoundException(
                    "Policy Not Found"));

        // Validation
        if (plan.getDurationInYears() == null) {
            throw new RuntimeException(
                    "Plan duration is missing");
        }

        if (!plan.isActive()) {
            throw new RuntimeException(
                    "Policy plan is inactive");
        }

        Policy policy =
                Policy.builder()
                .policyNumber(
                        "POL-" +
                        UUID.randomUUID()
                                .toString()
                                .substring(0, 8)
                                .toUpperCase())
                .startDate(LocalDate.now())
                .endDate(
                        LocalDate.now()
                                .plusYears(
                                        plan.getDurationInYears()))
                .status(
                        PolicyStatus.PENDING_PAYMENT)
                .customer(customer)
                .plan(plan)
                .build();

        policyRepository.save(policy);

        return new PolicyResponse(
                policy.getPolicyId(),
                policy.getPolicyNumber(),
                customer.getUser().getFullName(),
                plan.getPlanName(),
                policy.getStatus().name());
    }

    @Override
    public List<PolicyResponse>
    getPoliciesByCustomer(Long customerId) {

        return policyRepository
                .findByCustomerCustomerId(customerId)
                .stream()
                .map(policy ->
                        new PolicyResponse(
                                policy.getPolicyId(),
                                policy.getPolicyNumber(),
                                policy.getCustomer()
                                        .getUser()
                                        .getFullName(),
                                policy.getPlan()
                                        .getPlanName(),
                                policy.getStatus()
                                        .name()))
                .toList();
    }
    @Override
    public PolicyResponse issuePolicy(
            Long policyId) {

        Policy policy =
                policyRepository.findById(policyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Policy Not Found"));

        policy.setStatus(
                PolicyStatus.ACTIVE);

        policyRepository.save(policy);

        return new PolicyResponse(
                policy.getPolicyId(),
                policy.getPolicyNumber(),
                policy.getCustomer()
                      .getUser()
                      .getFullName(),
                policy.getPlan()
                      .getPlanName(),
                policy.getStatus()
                      .name());
    }
    
    @Override
    public PolicyResponse cancelPolicy(
            Long policyId) {

        Policy policy =
                policyRepository.findById(policyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Policy Not Found"));

        policy.setStatus(
                PolicyStatus.CANCELLED);

        policyRepository.save(policy);

        return new PolicyResponse(
                policy.getPolicyId(),
                policy.getPolicyNumber(),
                policy.getCustomer()
                      .getUser()
                      .getFullName(),
                policy.getPlan()
                      .getPlanName(),
                policy.getStatus()
                      .name());
    }
    
    @Override
    public PolicyResponse getPolicyById(
            Long policyId) {

        Policy policy =
                policyRepository.findById(policyId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Policy not found"));

        return new PolicyResponse(
                policy.getPolicyId(),
                policy.getPolicyNumber(),
                policy.getCustomer()
                        .getUser()
                        .getFullName(),
                policy.getPlan()
                        .getPlanName(),
                policy.getStatus()
                        .name());
    }
    
    @Override
    public List<PolicyResponse>
    getAllPolicies() {

        return policyRepository.findAll()
                .stream()
                .map(policy ->
                        new PolicyResponse(
                                policy.getPolicyId(),
                                policy.getPolicyNumber(),
                                policy.getCustomer()
                                        .getUser()
                                        .getFullName(),
                                policy.getPlan()
                                        .getPlanName(),
                                policy.getStatus()
                                        .name()))
                .toList();
    }
    
    
    
}