package com.insurance.demo.serviceImpl;

import com.insurance.demo.dto.PagedResponse;
import com.insurance.demo.dto.PaymentRequest;
import com.insurance.demo.dto.PaymentResponse;
import com.insurance.demo.entity.Customer;
import com.insurance.demo.entity.Policy;
import com.insurance.demo.entity.PremiumPayment;
import com.insurance.demo.enums.PaymentStatus;
import com.insurance.demo.enums.PolicyStatus;
import com.insurance.demo.exception.BadRequestException;
import com.insurance.demo.exception.DuplicateResourceException;
import com.insurance.demo.exception.ResourceNotFoundException;
import com.insurance.demo.exception.UnauthorizedAccessException;
import com.insurance.demo.repository.CustomerRepository;
import com.insurance.demo.repository.PaymentRepository;
import com.insurance.demo.repository.PolicyRepository;
import com.insurance.demo.service.PaymentService;
import com.insurance.demo.util.PaginationValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("paymentDate", "amount", "paymentStatus");

    private final PaymentRepository paymentRepository;
    private final PolicyRepository policyRepository;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    public PaymentResponse makePayment(PaymentRequest request, Long userId, String role) {
        log.info("Processing payment for policyId={} by userId={}", request.getPolicyId(), userId);

        // Check duplicate transaction reference
        if (paymentRepository.existsByTransactionReference(request.getTransactionReference())) {
            log.warn("Duplicate transaction reference: {}", request.getTransactionReference());
            throw new DuplicateResourceException("Transaction reference already exists: " + request.getTransactionReference());
        }

        Policy policy = policyRepository.findById(request.getPolicyId())
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found with ID: " + request.getPolicyId()));

        // CUSTOMER can only pay for their own policies
        if ("CUSTOMER".equals(role)) {
            Customer customer = customerRepository.findByUser_Id(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found"));
            if (!policy.getCustomer().getCustomerId().equals(customer.getCustomerId())) {
                throw new UnauthorizedAccessException("You are not authorized to make payment for this policy");
            }
        }

        PremiumPayment payment = PremiumPayment.builder()
                .policy(policy)
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMode())
                .transactionReference(request.getTransactionReference())
                .paymentStatus(request.getPaymentStatus())
                .build();

        paymentRepository.save(payment);

        // Only SUCCESS payments activate the policy and update totalPremiumPaid
        if (request.getPaymentStatus() == PaymentStatus.SUCCESS) {
            policy.setTotalPremiumPaid(policy.getTotalPremiumPaid() + request.getAmount());

            // Activate if total paid >= plan premium
            if (policy.getTotalPremiumPaid() >= policy.getPlan().getPremiumAmount()) {
                policy.setStatus(PolicyStatus.ACTIVE);
                log.info("Policy activated after payment: policyId={}, totalPaid={}", policy.getPolicyId(), policy.getTotalPremiumPaid());
            }

            policyRepository.save(policy);
        } else {
            log.warn("Payment status is {} — policy status unchanged for policyId={}", request.getPaymentStatus(), policy.getPolicyId());
        }

        log.info("Payment recorded: paymentId={}, txnRef={}, status={}", payment.getPaymentId(), payment.getTransactionReference(), payment.getPaymentStatus());
        return mapToResponse(payment);
    }

    @Override
    public PagedResponse<PaymentResponse> getPaymentsByPolicy(Long policyId, int page, int size) {
        PaginationValidator.validate(page, size, "paymentDate", ALLOWED_SORT_FIELDS);
        Pageable pageable = PageRequest.of(page, size, Sort.by("paymentDate").descending());
        Page<PremiumPayment> paymentPage = paymentRepository.findByPolicyPolicyId(policyId, pageable);
        return toPagedResponse(paymentPage);
    }

    @Override
    public PagedResponse<PaymentResponse> getAllPayments(int page, int size, String sortBy, String sortDir) {
        PaginationValidator.validate(page, size, sortBy, ALLOWED_SORT_FIELDS);
        Sort sort = "desc".equalsIgnoreCase(sortDir)
                ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PremiumPayment> paymentPage = paymentRepository.findAll(pageable);
        return toPagedResponse(paymentPage);
    }

    private PagedResponse<PaymentResponse> toPagedResponse(Page<PremiumPayment> page) {
        List<PaymentResponse> records = page.getContent().stream().map(this::mapToResponse).toList();
        return PagedResponse.<PaymentResponse>builder()
                .records(records)
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .totalRecords(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isLastPage(page.isLast())
                .build();
    }

    private PaymentResponse mapToResponse(PremiumPayment p) {
        return PaymentResponse.builder()
                .paymentId(p.getPaymentId())
                .policyId(p.getPolicy().getPolicyId())
                .policyNumber(p.getPolicy().getPolicyNumber())
                .amount(p.getAmount())
                .paymentMode(p.getPaymentMethod().name())
                .transactionReference(p.getTransactionReference())
                .paymentStatus(p.getPaymentStatus().name())
                .paymentDate(p.getPaymentDate())
                .build();
    }
}