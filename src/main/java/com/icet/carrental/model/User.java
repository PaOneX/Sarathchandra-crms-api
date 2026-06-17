package com.icet.carrental.model;

import com.icet.carrental.enums.AuthProvider;
import com.icet.carrental.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long          id;
    private String        name;
    private String        email;
    private String        password;
    private AuthProvider  authProvider;
    private String        googleId;
    private String        phone;
    private UserRole      role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
