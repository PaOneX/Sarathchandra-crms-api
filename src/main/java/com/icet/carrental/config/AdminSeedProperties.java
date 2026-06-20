package com.icet.carrental.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.admin")
public class AdminSeedProperties {

    private boolean enabled = false;
    private String  email;
    private String  password;
    private String  name    = "System Admin";
}
