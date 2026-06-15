package com.icet.carrental.dto.response;

import com.icet.carrental.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {
    private Long          id;
    private String        name;
    private String        email;
    private String        phone;
    private UserRole      role;
    private LocalDateTime createdAt;
}
