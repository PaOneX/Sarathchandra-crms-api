package com.icet.carrental.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icet.carrental.dto.request.GoogleAuthRequest;
import com.icet.carrental.dto.request.LoginRequest;
import com.icet.carrental.dto.request.RegisterRequest;
import com.icet.carrental.dto.response.AuthResponse;
import com.icet.carrental.enums.UserRole;
import com.icet.carrental.security.JwtUtil;
import com.icet.carrental.security.UserDetailsServiceImpl;
import com.icet.carrental.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void register_returnsCreated() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Jane Doe");
        request.setEmail("jane@example.com");
        request.setPassword("password123");
        request.setPhone("1234567890");

        AuthResponse authResponse = AuthResponse.builder()
                .userId(1L)
                .name("Jane Doe")
                .email("jane@example.com")
                .role(UserRole.CUSTOMER)
                .token("jwt-token")
                .tokenType("Bearer")
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("jwt-token"));
    }

    @Test
    void login_returnsOk() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("jane@example.com");
        request.setPassword("password123");

        AuthResponse authResponse = AuthResponse.builder()
                .userId(1L)
                .email("jane@example.com")
                .role(UserRole.CUSTOMER)
                .token("jwt-token")
                .tokenType("Bearer")
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").value("jwt-token"));
    }

    @Test
    void googleAuth_returnsOk() throws Exception {
        GoogleAuthRequest request = new GoogleAuthRequest();
        request.setIdToken("google-id-token");

        AuthResponse authResponse = AuthResponse.builder()
                .userId(2L)
                .email("google@example.com")
                .role(UserRole.CUSTOMER)
                .token("google-jwt")
                .tokenType("Bearer")
                .build();

        when(authService.googleAuth(any(GoogleAuthRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").value("google-jwt"));
    }
}
