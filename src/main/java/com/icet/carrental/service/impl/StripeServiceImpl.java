package com.icet.carrental.service.impl;

import com.icet.carrental.config.StripeProperties;
import com.icet.carrental.model.Booking;
import com.icet.carrental.service.StripeService;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class StripeServiceImpl implements StripeService {

    private static final String CHECKOUT_COMPLETED = "checkout.session.completed";

    private final StripeProperties stripeProperties;

    @Override
    public StripeCheckoutResult createCheckoutSession(Long paymentId, Booking booking) {
        ensureConfigured();

        Stripe.apiKey = stripeProperties.getSecretKey();

        long amountInCents = booking.getTotalAmount()
                .movePointRight(2)
                .longValueExact();

        try {
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(stripeProperties.getSuccessUrl() + "?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(stripeProperties.getCancelUrl())
                    .putMetadata("bookingId", String.valueOf(booking.getId()))
                    .putMetadata("paymentId", String.valueOf(paymentId))
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("usd")
                                                    .setUnitAmount(amountInCents)
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("Car Rental Booking #" + booking.getId())
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();

            Session session = Session.create(params);
            return new StripeCheckoutResult(session.getUrl(), session.getId());
        } catch (StripeException ex) {
            throw new IllegalStateException("Failed to create Stripe checkout session: " + ex.getMessage(), ex);
        }
    }

    @Override
    public StripeWebhookPayload parseWebhookEvent(String payload, String signatureHeader) {
        ensureWebhookConfigured();

        Event event;
        try {
            event = Webhook.constructEvent(payload, signatureHeader, stripeProperties.getWebhookSecret());
        } catch (SignatureVerificationException ex) {
            throw new IllegalArgumentException("Invalid Stripe webhook signature");
        }

        if (!CHECKOUT_COMPLETED.equals(event.getType())) {
            log.debug("Ignoring Stripe event type: {}", event.getType());
            return null;
        }

        Session session = (Session) event.getDataObjectDeserializer()
                .getObject()
                .filter(Session.class::isInstance)
                .orElseThrow(() -> new IllegalArgumentException("Invalid checkout session payload"));

        String paymentIdValue = session.getMetadata().get("paymentId");
        String bookingIdValue = session.getMetadata().get("bookingId");

        if (!StringUtils.hasText(paymentIdValue) || !StringUtils.hasText(bookingIdValue)) {
            throw new IllegalArgumentException("Stripe session metadata is missing payment or booking ID");
        }

        return new StripeWebhookPayload(
                Long.parseLong(paymentIdValue),
                Long.parseLong(bookingIdValue),
                session.getId()
        );
    }

    private void ensureConfigured() {
        if (!StringUtils.hasText(stripeProperties.getSecretKey())
                || !StringUtils.hasText(stripeProperties.getSuccessUrl())
                || !StringUtils.hasText(stripeProperties.getCancelUrl())) {
            throw new IllegalStateException("Stripe is not configured");
        }
    }

    private void ensureWebhookConfigured() {
        if (!StringUtils.hasText(stripeProperties.getWebhookSecret())) {
            throw new IllegalStateException("Stripe webhook secret is not configured");
        }
    }
}
