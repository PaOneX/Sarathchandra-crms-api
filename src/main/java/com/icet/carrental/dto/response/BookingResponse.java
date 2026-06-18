package com.icet.carrental.dto.response;

import com.icet.carrental.enums.BookingStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class BookingResponse {
    private Long          id;
    private Long          userId;
    private String        customerName;
    private Long          carId;
    private String        carBrand;
    private String        carModel;
    private LocalDate     startDate;
    private LocalDate     endDate;
    private BigDecimal    totalAmount;
    private BookingStatus status;
    private LocalDateTime createdAt;
    private BigDecimal    advanceAmount;
    private BigDecimal    balanceDue;
    private Boolean       whatsappSent;
}
