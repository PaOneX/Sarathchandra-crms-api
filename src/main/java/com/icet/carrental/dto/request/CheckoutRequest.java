package com.icet.carrental.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckoutRequest {

    @NotNull(message = "Booking ID is required")
    private Long bookingId;
}
