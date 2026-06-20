package com.icet.carrental.controller;

import com.icet.carrental.config.RestClientConfig;
import com.icet.carrental.service.PaymentService;
import com.icet.carrental.service.StripeService;
import com.icet.carrental.security.JwtAuthFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StripeWebhookController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Import(RestClientConfig.class)
class StripeWebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StripeService stripeService;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    void handleStripeWebhook_completesPaymentWhenEventIsValid() throws Exception {
        when(stripeService.parseWebhookEvent("payload", "sig"))
                .thenReturn(new StripeService.StripeWebhookPayload(99L, 7L, "cs_test_123"));

        mockMvc.perform(post("/api/webhooks/stripe")
                        .content("payload")
                        .header("Stripe-Signature", "sig")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("received"));

        verify(paymentService).completePaymentFromWebhook(99L, 7L, "cs_test_123");
    }

    @Test
    void handleStripeWebhook_ignoresUnhandledEvents() throws Exception {
        when(stripeService.parseWebhookEvent("payload", "sig")).thenReturn(null);

        mockMvc.perform(post("/api/webhooks/stripe")
                        .content("payload")
                        .header("Stripe-Signature", "sig")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("received"));
    }
}
