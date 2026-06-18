package com.icet.carrental.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "supabase")
public class SupabaseProperties {

    private String url;
    private String serviceRoleKey;
    private StorageBuckets storage = new StorageBuckets();

    @Data
    public static class StorageBuckets {
        private String cars    = "car-images";
        private String avatars = "avatars";
    }
}
