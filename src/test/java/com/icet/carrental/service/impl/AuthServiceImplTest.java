package com.icet.carrental.service.impl;

import com.icet.carrental.dto.request.GoogleAuthRequest;
import com.icet.carrental.dto.request.LoginRequest;
import com.icet.carrental.dto.request.RegisterRequest;
import com.icet.carrental.dto.response.AuthResponse;
import com.icet.carrental.enums.AuthProvider;
import com.icet.carrental.enums.UserRole;
import com.icet.carrental.model.User;
import com.icet.carrental.repository.UserRepository;
import com.icet.carrental.security.GoogleTokenVerifier;
import com.icet.carrental.security.GoogleUserInfo;
import com.icet.carrental.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository        userRepository;
    @Mock private PasswordEncoder       passwordEncoder;
    @Mock private JwtUtil               jwtUtil;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private GoogleTokenVerifier   googleTokenVerifier;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void register_createsLocalUserAndReturnsToken() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Jane Doe");
        request.setEmail("jane@example.com");
        request.setPassword("password123");
        request.setPhone("1234567890");

        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        when(jwtUtil.generateToken("jane@example.com", UserRole.CUSTOMER.name()))
                .thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertThat(response.getEmail()).isEqualTo("jane@example.com");
        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getRole()).isEqualTo(UserRole.CUSTOMER);
    }

    @Test
    void register_throwsWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("taken@example.com");

        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already registered");
    }

    @Test
    void login_returnsTokenForValidCredentials() {
        LoginRequest request = new LoginRequest();
        request.setEmail("jane@example.com");
        request.setPassword("password123");

        User user = User.builder()
                .id(1L)
                .name("Jane Doe")
                .email("jane@example.com")
                .role(UserRole.CUSTOMER)
                .authProvider(AuthProvider.LOCAL)
                .build();

        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("jane@example.com", UserRole.CUSTOMER.name()))
                .thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        verify(authenticationManager).authenticate(any());
        assertThat(response.getToken()).isEqualTo("jwt-token");
    }

    @Test
    void googleAuth_createsNewGoogleUser() {
        GoogleAuthRequest request = new GoogleAuthRequest();
        request.setIdToken("google-id-token");

        GoogleUserInfo googleUser = new GoogleUserInfo("google-sub-1", "new@example.com", "New User");

        when(googleTokenVerifier.verify("google-id-token")).thenReturn(googleUser);
        when(userRepository.findByGoogleId("google-sub-1")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2L);
            return user;
        });
        when(jwtUtil.generateToken("new@example.com", UserRole.CUSTOMER.name()))
                .thenReturn("google-jwt");

        AuthResponse response = authService.googleAuth(request);

        assertThat(response.getEmail()).isEqualTo("new@example.com");
        assertThat(response.getToken()).isEqualTo("google-jwt");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void googleAuth_linksExistingLocalUserByEmail() {
        GoogleAuthRequest request = new GoogleAuthRequest();
        request.setIdToken("google-id-token");

        GoogleUserInfo googleUser = new GoogleUserInfo("google-sub-2", "local@example.com", "Local User");

        User existingUser = User.builder()
                .id(3L)
                .name("Local User")
                .email("local@example.com")
                .password("encoded")
                .authProvider(AuthProvider.LOCAL)
                .role(UserRole.CUSTOMER)
                .build();

        when(googleTokenVerifier.verify("google-id-token")).thenReturn(googleUser);
        when(userRepository.findByGoogleId("google-sub-2")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("local@example.com")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        when(jwtUtil.generateToken("local@example.com", UserRole.CUSTOMER.name()))
                .thenReturn("linked-jwt");

        AuthResponse response = authService.googleAuth(request);

        assertThat(response.getToken()).isEqualTo("linked-jwt");
        assertThat(existingUser.getGoogleId()).isEqualTo("google-sub-2");
        assertThat(existingUser.getAuthProvider()).isEqualTo(AuthProvider.GOOGLE);
        verify(userRepository).save(existingUser);
    }
}
