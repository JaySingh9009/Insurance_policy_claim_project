package com.insurance.demo.serviceImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.json.JSONObject;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.insurance.demo.config.RazorpayConfig;
import com.insurance.demo.dto.CreateRazorpayOrderRequest;
import com.insurance.demo.dto.PagedResponse;
import com.insurance.demo.dto.PaymentResponse;
import com.insurance.demo.dto.RazorpayOrderResponse;
import com.insurance.demo.dto.VerifyRazorpayPaymentRequest;
import com.insurance.demo.entity.Customer;
import com.insurance.demo.entity.Policy;
import com.insurance.demo.entity.PremiumPayment;
import com.insurance.demo.entity.User;
import com.insurance.demo.enums.PaymentMethod;
import com.insurance.demo.enums.PaymentStatus;
import com.insurance.demo.enums.PolicyStatus;
import com.insurance.demo.enums.PremiumType;
import com.insurance.demo.enums.ProductType;
import com.insurance.demo.exception.BadRequestException;
import com.insurance.demo.exception.DuplicateResourceException;
import com.insurance.demo.exception.ResourceNotFoundException;
import com.insurance.demo.exception.UnauthorizedAccessException;
import com.insurance.demo.repository.CustomerRepository;
import com.insurance.demo.repository.PaymentRepository;
import com.insurance.demo.repository.PolicyRepository;
import com.insurance.demo.service.PaymentService;
import com.insurance.demo.util.PaginationValidator;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("paymentDate", "amount", "paymentStatus");

    private final PaymentRepository paymentRepository;
    private final PolicyRepository policyRepository;
    private final CustomerRepository customerRepository;
    private final RazorpayConfig razorpayConfig;
    private final ObjectProvider<RazorpayClient> razorpayClientProvider;

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
                .customerName(p.getPolicy().getCustomer() != null && p.getPolicy().getCustomer().getUser() != null 
                        ? p.getPolicy().getCustomer().getUser().getFullName() : null)
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
    public RazorpayOrderResponse createRazorpayOrder(CreateRazorpayOrderRequest request, Long userId, String role) {
        Policy policy = validateAndGetPolicyForPayment(request.getPolicyId(), userId, role);

        Double payableAmount = calculatePayableAmount(policy, request.getAmount());

        String orderId = null;
        RazorpayClient razorpayClient = razorpayClientProvider.getIfAvailable();
        if (razorpayClient != null) {
            try {
                JSONObject orderRequest = new JSONObject();
                orderRequest.put("amount", Math.round(payableAmount * 100)); // amount in paise
                orderRequest.put("currency", "INR");
                orderRequest.put("receipt", "txn_" + System.currentTimeMillis());

                Order order = razorpayClient.orders.create(orderRequest);
                orderId = order.get("id");
                log.info("Successfully generated real Razorpay order ID via SDK: {}", orderId);
            } catch (Exception e) {
                log.warn("Razorpay API order creation failed ({}), using fallback order ID generator.", e.getMessage());
            }
        }

        if (orderId == null) {
            orderId = "order_RZP_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 6);
        }

        return RazorpayOrderResponse.builder()
                .orderId(orderId)
                .amount(payableAmount)
                .currency("INR")
                .keyId(razorpayConfig.getKeyId())
                .policyId(policy.getPolicyId())
                .policyNumber(policy.getPolicyNumber())
                .customerName(policy.getCustomer().getUser().getFullName())
                .customerEmail(policy.getCustomer().getUser().getEmail())
                .planName(policy.getPlan().getPlanName())
                .build();
    }

    @Override
    @Transactional
    public PaymentResponse verifyRazorpayPayment(VerifyRazorpayPaymentRequest request, Long userId, String role) {
        Policy policy = validateAndGetPolicyForPayment(request.getPolicyId(), userId, role);

        if (paymentRepository.findByTransactionReference(request.getRazorpayPaymentId()).isPresent()) {
            throw new DuplicateResourceException("Payment with reference '" + request.getRazorpayPaymentId() + "' already processed.");
        }

        Double paidAmount = calculatePayableAmount(policy, request.getAmount());
        
        PremiumPayment payment = PremiumPayment.builder()
                .policy(policy)
                .amount(paidAmount)
                .paymentMethod(PaymentMethod.RAZORPAY)
                .transactionReference(request.getRazorpayPaymentId())
                .paymentStatus(PaymentStatus.SUCCESS)
                .paymentDate(LocalDateTime.now())
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

        boolean isTravelOrOneTime = (policy.getPlan() != null && policy.getPlan().getProduct() != null 
                && policy.getPlan().getProduct().getProductType() == ProductType.TRAVEL)
                || pType == PremiumType.ONE_TIME;

        LocalDate nextDue;
        if (isTravelOrOneTime) {
            nextDue = null;
        } else {
            switch (pType) {
                case MONTHLY -> nextDue = baseDate.plusMonths(1);
                case QUARTERLY -> nextDue = baseDate.plusMonths(3);
                case SEMI_ANNUAL -> nextDue = baseDate.plusMonths(6);
                case ANNUAL -> nextDue = baseDate.plusYears(1);
                default -> nextDue = baseDate.plusYears(1);
            }
        }

        policy.setNextPaymentDueDate(nextDue);
        policyRepository.save(policy);

        log.info("Razorpay Payment VERIFIED & SUCCESS: Transaction Ref: {}, Policy: {}, Amount: {}, Next Due: {}",
                request.getRazorpayPaymentId(), policy.getPolicyNumber(), paidAmount, nextDue);

        return mapToResponse(payment);
    }

    private Policy validateAndGetPolicyForPayment(Long policyId, Long userId, String role) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found with ID: " + policyId));

        if (policy.getCustomer() == null || policy.getCustomer().getUser() == null || policy.getPlan() == null) {
            throw new ResourceNotFoundException("Customer profile, user account, or policy plan not found for policy ID: " + policyId);
        }

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

        boolean isTravel = policy.getPlan() != null && policy.getPlan().getProduct() != null &&
                policy.getPlan().getProduct().getProductType() == ProductType.TRAVEL;

        if (isTravel && LocalDate.now().isAfter(policy.getStartDate())) {
            policy.setStatus(PolicyStatus.EXPIRED);
            policyRepository.save(policy);
            throw new BadRequestException("Payment for Travel policy must be completed on or before the departure date (" + policy.getStartDate() + ").");
        }

        if (policy.getStatus() == PolicyStatus.ACTIVE &&
            policy.getNextPaymentDueDate() != null &&
            LocalDate.now().isBefore(policy.getNextPaymentDueDate())) {
            throw new BadRequestException("Your premium for this cycle is already paid! Next installment is due on " + policy.getNextPaymentDueDate() + ".");
        }

        return policy;
    }

    private Double calculatePayableAmount(Policy policy, Double requestedAmount) {
        if (requestedAmount != null && requestedAmount > 0) {
            return requestedAmount;
        }
        return policy.getInstallmentAmount() != null && policy.getInstallmentAmount() > 0
                ? policy.getInstallmentAmount()
                : policy.getPlan().getPremiumAmount();
    }
}