package com.icet.carrental.service.impl;

import com.icet.carrental.dto.request.PaymentRequest;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository    userRepository;

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
    public PaymentResponse processPayment(PaymentRequest request, String email) {
        Booking booking = findBookingOrThrow(request.getBookingId());
        User    user    = findUserByEmailOrThrow(email);

        if (!booking.getUserId().equals(user.getId())) {
            throw new UnauthorizedException("This booking does not belong to you");
        }
        if (booking.getStatus() != BookingStatus.APPROVED) {
            throw new IllegalArgumentException(
                    "Payment can only be made for APPROVED bookings");
        }
        if (paymentRepository.findByBookingId(request.getBookingId()).isPresent()) {
            throw new IllegalArgumentException("Payment already exists for this booking");
        }

        Payment payment = Payment.builder()
                .bookingId(request.getBookingId())
                .amount(booking.getTotalAmount())
                .paymentMethod(request.getPaymentMethod())
                .transactionId(request.getTransactionId())
                .status(PaymentStatus.COMPLETED)
                .paidAt(LocalDateTime.now())
                .build();

        Payment saved = paymentRepository.save(payment);
        bookingRepository.updateStatus(booking.getId(), BookingStatus.COMPLETED);

        return toPaymentResponse(saved);
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
