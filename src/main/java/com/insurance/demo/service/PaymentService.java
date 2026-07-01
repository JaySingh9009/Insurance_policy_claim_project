package com.insurance.demo.service;

import com.insurance.demo.dto.PagedResponse;
import com.insurance.demo.dto.PaymentRequest;
import com.insurance.demo.dto.PaymentResponse;

public interface PaymentService {
    PaymentResponse makePayment(PaymentRequest request, Long userId, String role);
    PagedResponse<PaymentResponse> getPaymentsByPolicy(Long policyId, int page, int size);
    PagedResponse<PaymentResponse> getAllPayments(int page, int size, String sortBy, String sortDir);
    PagedResponse<PaymentResponse> getMyPayments(
            Long userId,
            int page,
            int size,
            String sortBy,
            String sortDir
    );
}