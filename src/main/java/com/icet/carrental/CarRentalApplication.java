package com.icet.carrental;

import com.icet.carrental.config.AdminSeedProperties;
import com.icet.carrental.config.CorsProperties;
import com.icet.carrental.config.StripeProperties;
import com.icet.carrental.config.SupabaseProperties;
import com.icet.carrental.config.WhatsAppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        AdminSeedProperties.class,
        CorsProperties.class,
        StripeProperties.class,
        SupabaseProperties.class,
        WhatsAppProperties.class
})
public class CarRentalApplication {

    public static void main(String[] args) {
        SpringApplication.run(CarRentalApplication.class, args);
    }
}
