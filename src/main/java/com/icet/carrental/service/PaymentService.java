package com.icet.carrental.service;

import com.icet.carrental.dto.request.CheckoutRequest;
import com.icet.carrental.dto.response.CheckoutSessionResponse;
import com.icet.carrental.dto.response.PaymentResponse;

import java.util.List;

public interface PaymentService {

    List<PaymentResponse> getAllPayments();

    List<PaymentResponse> getMyPayments(String email);

    PaymentResponse getPaymentByBookingId(Long bookingId, String email, boolean isPrivileged);

    CheckoutSessionResponse createCheckoutSession(CheckoutRequest request, String email);

    void completePaymentFromWebhook(Long paymentId, Long bookingId, String sessionId);
}
