package com.icet.carrental.dto.response;

import com.icet.carrental.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponse {
    private Long          id;
    private Long          bookingId;
    private BigDecimal    amount;
    private String        paymentMethod;
    private String        transactionId;
    private PaymentStatus status;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}
