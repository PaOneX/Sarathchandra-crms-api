package com.icet.carrental.config;

import com.icet.carrental.service.storage.LocalFileStorageService;
import com.icet.carrental.service.storage.StorageService;
import com.icet.carrental.service.storage.SupabaseStorageService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StorageConfig implements WebMvcConfigurer {

    @Bean
    public StorageService storageService(SupabaseProperties properties, RestClient restClient) {
        if (isSupabaseConfigured(properties)) {
            return new SupabaseStorageService(properties, restClient);
        }
        return new LocalFileStorageService();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/api/uploads/**")
                .addResourceLocations("file:uploads/");
    }

    private boolean isSupabaseConfigured(SupabaseProperties properties) {
        return StringUtils.hasText(properties.getServiceRoleKey())
                && StringUtils.hasText(properties.getUrl())
                && !properties.getUrl().contains("your-project");
    }
}
