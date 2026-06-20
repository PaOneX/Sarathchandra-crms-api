package com.icet.carrental.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckoutSessionResponse {

    private String checkoutUrl;
    private String sessionId;
    private Long   paymentId;
}
