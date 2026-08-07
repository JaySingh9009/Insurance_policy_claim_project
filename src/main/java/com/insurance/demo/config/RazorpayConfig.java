package com.insurance.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class RazorpayConfig {

    @Value("${razorpay.key.id:rzp_test_THPAh3J7KnVXXJ}")
    private String keyId;

    @Value("${razorpay.key.secret:dummy_razorpay_secret_key_12345}")
    private String keySecret;

    @Bean
    public RazorpayClient razorpayClient() {
        try {
            return new RazorpayClient(keyId, keySecret);
        } catch (RazorpayException e) {
            log.error("Failed to initialize RazorpayClient bean: {}", e.getMessage());
            return null;
        }
    }
}
