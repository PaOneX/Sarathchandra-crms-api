package com.icet.carrental.controller;

import com.icet.carrental.service.PaymentService;
import com.icet.carrental.service.StripeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class StripeWebhookController {

    private final StripeService  stripeService;
    private final PaymentService paymentService;

    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {

        StripeService.StripeWebhookPayload webhookPayload =
                stripeService.parseWebhookEvent(payload, signature);

        if (webhookPayload != null) {
            paymentService.completePaymentFromWebhook(
                    webhookPayload.paymentId(),
                    webhookPayload.bookingId(),
                    webhookPayload.sessionId()
            );
        }

        return ResponseEntity.ok("received");
    }
}
