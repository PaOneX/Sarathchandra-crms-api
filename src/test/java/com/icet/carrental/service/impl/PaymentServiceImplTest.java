package com.icet.carrental.service.impl;

import com.icet.carrental.dto.request.CheckoutRequest;
import com.icet.carrental.dto.response.CheckoutSessionResponse;
import com.icet.carrental.enums.BookingStatus;
import com.icet.carrental.enums.PaymentStatus;
import com.icet.carrental.model.Booking;
import com.icet.carrental.model.Payment;
import com.icet.carrental.model.User;
import com.icet.carrental.repository.BookingRepository;
import com.icet.carrental.repository.PaymentRepository;
import com.icet.carrental.repository.UserRepository;
import com.icet.carrental.service.StripeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private UserRepository    userRepository;
    @Mock private StripeService     stripeService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void createCheckoutSession_createsPendingPaymentAndReturnsUrl() {
        CheckoutRequest request = new CheckoutRequest();
        request.setBookingId(7L);

        Booking booking = Booking.builder()
                .id(7L)
                .userId(3L)
                .status(BookingStatus.APPROVED)
                .totalAmount(BigDecimal.valueOf(200))
                .build();

        User user = User.builder().id(3L).email("customer@example.com").build();

        when(bookingRepository.findById(7L)).thenReturn(Optional.of(booking));
        when(userRepository.findByEmail("customer@example.com")).thenReturn(Optional.of(user));
        when(paymentRepository.findByBookingId(7L)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(99L);
            return payment;
        });
        when(stripeService.createCheckoutSession(99L, booking))
                .thenReturn(new StripeService.StripeCheckoutResult(
                        "https://checkout.stripe.com/pay/cs_test_123",
                        "cs_test_123"
                ));

        CheckoutSessionResponse response = paymentService.createCheckoutSession(request, "customer@example.com");

        assertThat(response.getCheckoutUrl()).contains("checkout.stripe.com");
        assertThat(response.getSessionId()).isEqualTo("cs_test_123");
        assertThat(response.getPaymentId()).isEqualTo(99L);
        verify(paymentRepository).updateTransactionId(99L, "cs_test_123");
    }

    @Test
    void createCheckoutSession_rejectsDuplicatePayment() {
        CheckoutRequest request = new CheckoutRequest();
        request.setBookingId(7L);

        Booking booking = Booking.builder()
                .id(7L)
                .userId(3L)
                .status(BookingStatus.APPROVED)
                .totalAmount(BigDecimal.valueOf(200))
                .build();

        User user = User.builder().id(3L).email("customer@example.com").build();

        when(bookingRepository.findById(7L)).thenReturn(Optional.of(booking));
        when(userRepository.findByEmail("customer@example.com")).thenReturn(Optional.of(user));
        when(paymentRepository.findByBookingId(7L)).thenReturn(Optional.of(
                Payment.builder().id(1L).bookingId(7L).status(PaymentStatus.PENDING).build()
        ));

        assertThatThrownBy(() -> paymentService.createCheckoutSession(request, "customer@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Payment already exists");
    }

    @Test
    void completePaymentFromWebhook_isIdempotentWhenAlreadyCompleted() {
        Payment payment = Payment.builder()
                .id(99L)
                .bookingId(7L)
                .status(PaymentStatus.COMPLETED)
                .build();

        when(paymentRepository.findById(99L)).thenReturn(Optional.of(payment));

        paymentService.completePaymentFromWebhook(99L, 7L, "cs_test_123");

        verify(paymentRepository, never()).updateStatus(any(), any(), any());
        verify(bookingRepository, never()).updateStatus(any(), any());
    }

    @Test
    void completePaymentFromWebhook_marksPaymentAndBookingCompleted() {
        Payment payment = Payment.builder()
                .id(99L)
                .bookingId(7L)
                .status(PaymentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        when(paymentRepository.findById(99L)).thenReturn(Optional.of(payment));

        paymentService.completePaymentFromWebhook(99L, 7L, "cs_test_123");

        verify(paymentRepository).updateTransactionId(99L, "cs_test_123");
        verify(paymentRepository).updateStatus(eq(99L), eq(PaymentStatus.COMPLETED), any());
        verify(bookingRepository).updateStatus(7L, BookingStatus.COMPLETED);
    }
}
