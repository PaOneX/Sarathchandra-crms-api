package com.icet.carrental.dto.response;

import com.icet.carrental.enums.UserRole;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private Long     userId;
    private String   name;
    private String   email;
    private UserRole role;
    private String   profilePictureUrl;
    private String   token;
    private String   tokenType;
}
