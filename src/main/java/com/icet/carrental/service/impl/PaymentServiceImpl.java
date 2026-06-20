package com.icet.carrental.service.impl;

import com.icet.carrental.dto.request.CheckoutRequest;
import com.icet.carrental.dto.response.CheckoutSessionResponse;
import com.icet.carrental.dto.response.PaymentResponse;
import com.icet.carrental.enums.BookingStatus;
import com.icet.carrental.enums.PaymentStatus;
import com.icet.carrental.exception.ResourceNotFoundException;
import com.icet.carrental.exception.UnauthorizedException;
import com.icet.carrental.model.Booking;
import com.icet.carrental.model.Payment;
import com.icet.carrental.model.User;
import com.icet.carrental.repository.BookingRepository;
import com.icet.carrental.repository.PaymentRepository;
import com.icet.carrental.repository.UserRepository;
import com.icet.carrental.service.PaymentService;
import com.icet.carrental.service.StripeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final String PAYMENT_METHOD_STRIPE = "STRIPE";

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository    userRepository;
    private final StripeService     stripeService;

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(this::toPaymentResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getMyPayments(String email) {
        User user = findUserByEmailOrThrow(email);
        return paymentRepository.findByUserId(user.getId()).stream()
                .map(this::toPaymentResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByBookingId(Long bookingId, String email, boolean isPrivileged) {
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found for booking: " + bookingId));

        if (!isPrivileged) {
            Booking booking = findBookingOrThrow(bookingId);
            User    user    = findUserByEmailOrThrow(email);
            if (!booking.getUserId().equals(user.getId())) {
                throw new UnauthorizedException("Access denied");
            }
        }

        return toPaymentResponse(payment);
    }

    @Override
    @Transactional
    public CheckoutSessionResponse createCheckoutSession(CheckoutRequest request, String email) {
        Booking booking = findBookingOrThrow(request.getBookingId());
        User    user    = findUserByEmailOrThrow(email);

        if (!booking.getUserId().equals(user.getId())) {
            throw new UnauthorizedException("This booking does not belong to you");
        }
        if (booking.getStatus() != BookingStatus.APPROVED) {
            throw new IllegalArgumentException("Payment can only be made for APPROVED bookings");
        }
        if (paymentRepository.findByBookingId(request.getBookingId()).isPresent()) {
            throw new IllegalArgumentException("Payment already exists for this booking");
        }

        Payment payment = Payment.builder()
                .bookingId(request.getBookingId())
                .amount(booking.getTotalAmount())
                .paymentMethod(PAYMENT_METHOD_STRIPE)
                .status(PaymentStatus.PENDING)
                .build();

        Payment saved = paymentRepository.save(payment);
        StripeService.StripeCheckoutResult checkout = stripeService.createCheckoutSession(saved.getId(), booking);
        paymentRepository.updateTransactionId(saved.getId(), checkout.sessionId());

        return CheckoutSessionResponse.builder()
                .checkoutUrl(checkout.checkoutUrl())
                .sessionId(checkout.sessionId())
                .paymentId(saved.getId())
                .build();
    }

    @Override
    @Transactional
    public void completePaymentFromWebhook(Long paymentId, Long bookingId, String sessionId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            log.info("Payment {} already completed; ignoring duplicate webhook", paymentId);
            return;
        }

        if (!payment.getBookingId().equals(bookingId)) {
            throw new IllegalArgumentException("Payment booking ID does not match webhook metadata");
        }

        paymentRepository.updateTransactionId(paymentId, sessionId);
        paymentRepository.updateStatus(paymentId, PaymentStatus.COMPLETED, LocalDateTime.now());
        bookingRepository.updateStatus(bookingId, BookingStatus.COMPLETED);

        log.info("Payment {} completed via Stripe for booking {}", paymentId, bookingId);
    }

    private Booking findBookingOrThrow(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", id));
    }

    private User findUserByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private PaymentResponse toPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .bookingId(payment.getBookingId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .transactionId(payment.getTransactionId())
                .status(payment.getStatus())
                .paidAt(payment.getPaidAt())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
