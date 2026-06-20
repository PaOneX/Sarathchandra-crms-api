package com.icet.carrental.service;

import com.icet.carrental.model.Booking;

public interface StripeService {

    StripeCheckoutResult createCheckoutSession(Long paymentId, Booking booking);

    StripeWebhookPayload parseWebhookEvent(String payload, String signatureHeader);

    record StripeCheckoutResult(String checkoutUrl, String sessionId) {}

    record StripeWebhookPayload(Long paymentId, Long bookingId, String sessionId) {}
}
