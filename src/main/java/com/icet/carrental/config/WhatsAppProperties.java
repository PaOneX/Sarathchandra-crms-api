package com.icet.carrental.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "whatsapp")
public class WhatsAppProperties {

    private String apiUrl = "https://graph.facebook.com/v19.0";
    private String phoneNumberId = "";
    private String accessToken = "";
    private int advancePercent = 30;
}
