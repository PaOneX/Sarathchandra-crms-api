package com.icet.carrental.model;

import com.icet.carrental.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    private Long          id;
    private Long          userId;
    private Long          carId;
    private LocalDate     startDate;
    private LocalDate     endDate;
    private BigDecimal    totalAmount;
    private BookingStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
