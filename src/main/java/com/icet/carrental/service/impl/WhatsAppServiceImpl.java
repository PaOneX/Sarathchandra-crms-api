package com.icet.carrental.service.impl;

import com.icet.carrental.config.WhatsAppProperties;
import com.icet.carrental.model.Booking;
import com.icet.carrental.model.Car;
import com.icet.carrental.model.User;
import com.icet.carrental.service.WhatsAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WhatsAppServiceImpl implements WhatsAppService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final RestClient restClient;
    private final WhatsAppProperties whatsAppProperties;

    @Override
    public boolean sendBookingConfirmation(User user, Booking booking, Car car) {
        if (!isConfigured()) {
            log.warn("WhatsApp API is not configured; skipping booking confirmation for booking {}", booking.getId());
            return false;
        }

        String recipient = normalizePhone(user.getPhone());
        if (!StringUtils.hasText(recipient)) {
            log.warn("User {} has no valid phone; skipping WhatsApp confirmation for booking {}", user.getId(), booking.getId());
            return false;
        }

        String body = buildBookingReport(user, booking, car);

        try {
            restClient.post()
                    .uri(whatsAppProperties.getApiUrl() + "/" + whatsAppProperties.getPhoneNumberId() + "/messages")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + whatsAppProperties.getAccessToken())
                    .body(Map.of(
                            "messaging_product", "whatsapp",
                            "to", recipient,
                            "type", "text",
                            "text", Map.of("body", body)
                    ))
                    .retrieve()
                    .toBodilessEntity();

            log.debug("WhatsApp booking confirmation sent for booking {} to {}", booking.getId(), recipient);
            return true;
        } catch (Exception ex) {
            log.error("Failed to send WhatsApp confirmation for booking {}: {}", booking.getId(), ex.getMessage());
            return false;
        }
    }

    private boolean isConfigured() {
        return StringUtils.hasText(whatsAppProperties.getPhoneNumberId())
                && StringUtils.hasText(whatsAppProperties.getAccessToken());
    }

    private String normalizePhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            return null;
        }
        return phone.replaceAll("\\D", "");
    }

    private String buildBookingReport(User user, Booking booking, Car car) {
        long days = ChronoUnit.DAYS.between(booking.getStartDate(), booking.getEndDate());
        BigDecimal total = booking.getTotalAmount();
        BigDecimal advance = total
                .multiply(BigDecimal.valueOf(whatsAppProperties.getAdvancePercent()))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal balance = total.subtract(advance);

        String yearSuffix = car.getYear() != null ? " (" + car.getYear() + ")" : "";

        return String.join("\n",
                "*DriveX — Booking Confirmation*",
                "",
                "Booking ID: #" + booking.getId(),
                "Customer: " + user.getName(),
                "Phone: " + (StringUtils.hasText(user.getPhone()) ? user.getPhone() : "—"),
                "",
                "*Vehicle*",
                car.getBrand() + " " + car.getModel() + yearSuffix,
                "Daily rate: $" + car.getDailyRate(),
                "",
                "*Rental period*",
                "Pick-up: " + booking.getStartDate().format(DATE_FMT),
                "Return: " + booking.getEndDate().format(DATE_FMT),
                "Duration: " + days + " day" + (days == 1 ? "" : "s"),
                "",
                "*Payment summary*",
                "Total: $" + total,
                "Advance due (" + whatsAppProperties.getAdvancePercent() + "%): $" + advance,
                "Balance due: $" + balance,
                "Status: " + booking.getStatus(),
                "",
                "Booked on: " + booking.getCreatedAt().format(DATE_FMT),
                "",
                "Reply to this chat or call us for changes."
        );
    }
}
