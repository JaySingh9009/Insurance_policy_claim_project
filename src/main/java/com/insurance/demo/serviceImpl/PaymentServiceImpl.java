package com.insurance.demo.serviceImpl;

import com.insurance.demo.dto.PagedResponse;
import com.insurance.demo.dto.PaymentRequest;
import com.insurance.demo.dto.PaymentResponse;
import com.insurance.demo.entity.Customer;
import com.insurance.demo.entity.Policy;
import com.insurance.demo.entity.PremiumPayment;
import com.insurance.demo.enums.PaymentStatus;
import com.insurance.demo.enums.PolicyStatus;
import com.insurance.demo.enums.PremiumType;
import com.insurance.demo.exception.BadRequestException;
import java.time.LocalDate;
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
                .customerName(p.getPolicy().getCustomer() != null && p.getPolicy().getCustomer().getUser() != null ? p.getPolicy().getCustomer().getUser().getFullName() : null)
                .amount(p.getAmount())
                .paymentMode(p.getPaymentMethod().name())
                .transactionReference(p.getTransactionReference())
                .paymentStatus(p.getPaymentStatus().name())
                .paymentDate(p.getPaymentDate())
                .build();
    }
    
    @Override
    public PagedResponse<PaymentResponse> getMyPayments(
            Long userId,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        PaginationValidator.validate(page, size, sortBy, ALLOWED_SORT_FIELDS);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDir.equalsIgnoreCase("desc")
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC,
                        sortBy)
        );

        Page<PremiumPayment> paymentPage =
                paymentRepository.findByPolicyCustomerUserId(userId, pageable);

        return toPagedResponse(paymentPage);
    }

    @Override
    @Transactional
    public com.insurance.demo.dto.RazorpayOrderResponse createRazorpayOrder(com.insurance.demo.dto.CreateRazorpayOrderRequest request, Long userId, String role) {
        Policy policy = policyRepository.findById(request.getPolicyId())
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found with ID: " + request.getPolicyId()));

        if ("CUSTOMER".equals(role)) {
            Customer customer = customerRepository.findByUser_Id(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found for user: " + userId));
            if (!policy.getCustomer().getCustomerId().equals(customer.getCustomerId())) {
                throw new UnauthorizedAccessException("You can only pay for your own policy.");
            }
        }

        if (policy.getStatus() == PolicyStatus.CANCELLED || policy.getStatus() == PolicyStatus.EXPIRED) {
            throw new BadRequestException("Cannot make payment on a " + policy.getStatus() + " policy.");
        }

        // Validate if premium is already paid for current cycle
        if (policy.getStatus() == PolicyStatus.ACTIVE &&
            policy.getNextPaymentDueDate() != null &&
            LocalDate.now().isBefore(policy.getNextPaymentDueDate())) {
            throw new BadRequestException("Your premium for this cycle is already paid! Next installment is due on " + policy.getNextPaymentDueDate() + ".");
        }

        Double payableAmount = request.getAmount() != null && request.getAmount() > 0
                ? request.getAmount()
                : (policy.getInstallmentAmount() != null ? policy.getInstallmentAmount() : policy.getPlan().getPremiumAmount());

        String orderId = "order_RZP_" + System.currentTimeMillis() + "_" + java.util.UUID.randomUUID().toString().substring(0, 6);

        String customerName = policy.getCustomer() != null && policy.getCustomer().getUser() != null
                ? policy.getCustomer().getUser().getFullName() : "Valued Customer";
        String customerEmail = policy.getCustomer() != null && policy.getCustomer().getUser() != null
                ? policy.getCustomer().getUser().getEmail() : "customer@insurance.com";

        return com.insurance.demo.dto.RazorpayOrderResponse.builder()
                .orderId(orderId)
                .amount(payableAmount)
                .currency("INR")
                .keyId("rzp_test_THPAh3J7KnVXXJ")
                .policyId(policy.getPolicyId())
                .policyNumber(policy.getPolicyNumber())
                .customerName(customerName)
                .customerEmail(customerEmail)
                .planName(policy.getPlan() != null ? policy.getPlan().getPlanName() : "Insurance Plan")
                .build();
    }

    @Override
    @Transactional
    public PaymentResponse verifyRazorpayPayment(com.insurance.demo.dto.VerifyRazorpayPaymentRequest request, Long userId, String role) {
        Policy policy = policyRepository.findById(request.getPolicyId())
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found with ID: " + request.getPolicyId()));

        if ("CUSTOMER".equals(role)) {
            Customer customer = customerRepository.findByUser_Id(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found for user: " + userId));
            if (!policy.getCustomer().getCustomerId().equals(customer.getCustomerId())) {
                throw new UnauthorizedAccessException("You can only pay for your own policy.");
            }
        }

        if (policy.getStatus() == PolicyStatus.CANCELLED || policy.getStatus() == PolicyStatus.EXPIRED) {
            throw new BadRequestException("Cannot make payment on a " + policy.getStatus() + " policy.");
        }

        if (policy.getStatus() == PolicyStatus.ACTIVE &&
            policy.getNextPaymentDueDate() != null &&
            LocalDate.now().isBefore(policy.getNextPaymentDueDate())) {
            throw new BadRequestException("Your premium for this cycle is already paid! Next installment is due on " + policy.getNextPaymentDueDate() + ".");
        }

        if (paymentRepository.findByTransactionReference(request.getRazorpayPaymentId()).isPresent()) {
            throw new DuplicateResourceException("Payment with reference '" + request.getRazorpayPaymentId() + "' already processed.");
        }

        Double paidAmount = request.getAmount() != null && request.getAmount() > 0
                ? request.getAmount()
                : (policy.getInstallmentAmount() != null ? policy.getInstallmentAmount() : policy.getPlan().getPremiumAmount());

        com.insurance.demo.enums.PaymentMethod method;
        try {
            method = com.insurance.demo.enums.PaymentMethod.valueOf(
                    request.getPaymentMethod() != null ? request.getPaymentMethod().toUpperCase() : "UPI"
            );
        } catch (IllegalArgumentException e) {
            method = com.insurance.demo.enums.PaymentMethod.UPI;
        }

        PremiumPayment payment = PremiumPayment.builder()
                .policy(policy)
                .amount(paidAmount)
                .paymentMethod(method)
                .transactionReference(request.getRazorpayPaymentId())
                .paymentStatus(PaymentStatus.SUCCESS)
                .paymentDate(java.time.LocalDateTime.now())
                .build();

        paymentRepository.save(payment);

        // Update Policy Status and Next Payment Due Date
        double updatedTotalPaid = (policy.getTotalPremiumPaid() != null ? policy.getTotalPremiumPaid() : 0.0) + paidAmount;
        policy.setTotalPremiumPaid(updatedTotalPaid);
        policy.setLastPaymentDate(LocalDate.now());

        // Reactivate LAPSED / INACTIVE / PENDING_PAYMENT policy back to ACTIVE
        policy.setStatus(PolicyStatus.ACTIVE);

        // Calculate next due date according to selected premium frequency
        LocalDate baseDate = policy.getNextPaymentDueDate() != null && policy.getNextPaymentDueDate().isAfter(LocalDate.now())
                ? policy.getNextPaymentDueDate()
                : LocalDate.now();

        PremiumType pType = policy.getSelectedPremiumType() != null
                ? policy.getSelectedPremiumType()
                : (policy.getPlan() != null ? policy.getPlan().getPremiumType() : PremiumType.ANNUAL);

        LocalDate nextDue;
        switch (pType) {
            case MONTHLY -> nextDue = baseDate.plusMonths(1);
            case QUARTERLY -> nextDue = baseDate.plusMonths(3);
            case SEMI_ANNUAL -> nextDue = baseDate.plusMonths(6);
            case ANNUAL -> nextDue = baseDate.plusYears(1);
            case ONE_TIME -> nextDue = policy.getEndDate();
            default -> nextDue = baseDate.plusYears(1);
        }

        policy.setNextPaymentDueDate(nextDue);
        policyRepository.save(policy);

        log.info("Razorpay Payment VERIFIED & SUCCESS: {} for policy {} (Status: ACTIVE, Next Due: {})",
                request.getRazorpayPaymentId(), policy.getPolicyNumber(), nextDue);

         return mapToResponse(payment);
    }
}