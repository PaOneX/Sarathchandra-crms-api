package com.icet.carrental.service;

import com.icet.carrental.dto.request.PaymentRequest;
import com.icet.carrental.dto.response.PaymentResponse;

import java.util.List;

public interface PaymentService {

    List<PaymentResponse> getAllPayments();

    List<PaymentResponse> getMyPayments(String email);

    PaymentResponse getPaymentByBookingId(Long bookingId, String email, boolean isPrivileged);

    PaymentResponse processPayment(PaymentRequest request, String email);
}
