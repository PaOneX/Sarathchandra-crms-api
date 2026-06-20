package com.icet.carrental.controller;

import com.icet.carrental.dto.request.CheckoutRequest;
import com.icet.carrental.dto.response.ApiResponse;
import com.icet.carrental.dto.response.CheckoutSessionResponse;
import com.icet.carrental.dto.response.PaymentResponse;
import com.icet.carrental.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/checkout")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CheckoutSessionResponse>> createCheckoutSession(
            @Valid @RequestBody CheckoutRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        CheckoutSessionResponse session = paymentService.createCheckoutSession(
                request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Checkout session created", session));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PAYMENT_MANAGER')")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getAllPayments() {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getAllPayments()));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getMyPayments(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                ApiResponse.success(paymentService.getMyPayments(userDetails.getUsername())));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByBookingId(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal UserDetails userDetails) {

        boolean isPrivileged = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ROLE_PAYMENT_MANAGER"));

        return ResponseEntity.ok(ApiResponse.success(
                paymentService.getPaymentByBookingId(
                        bookingId, userDetails.getUsername(), isPrivileged)));
    }
}
