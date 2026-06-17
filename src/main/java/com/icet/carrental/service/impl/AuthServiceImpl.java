package com.icet.carrental.service.impl;

import com.icet.carrental.dto.request.GoogleAuthRequest;
import com.icet.carrental.dto.request.LoginRequest;
import com.icet.carrental.dto.request.RegisterRequest;
import com.icet.carrental.dto.response.AuthResponse;
import com.icet.carrental.enums.AuthProvider;
import com.icet.carrental.enums.UserRole;
import com.icet.carrental.exception.ResourceNotFoundException;
import com.icet.carrental.model.User;
import com.icet.carrental.repository.UserRepository;
import com.icet.carrental.security.GoogleTokenVerifier;
import com.icet.carrental.security.GoogleUserInfo;
import com.icet.carrental.security.JwtUtil;
import com.icet.carrental.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository        userRepository;
    private final PasswordEncoder       passwordEncoder;
    private final JwtUtil               jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final GoogleTokenVerifier   googleTokenVerifier;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .authProvider(AuthProvider.LOCAL)
                .phone(request.getPhone())
                .role(UserRole.CUSTOMER)
                .build();

        User saved = userRepository.save(user);
        String token = jwtUtil.generateToken(saved.getEmail(), saved.getRole().name());

        return buildAuthResponse(saved, token);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", 0L));

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return buildAuthResponse(user, token);
    }

    @Override
    @Transactional
    public AuthResponse googleAuth(GoogleAuthRequest request) {
        GoogleUserInfo googleUser = googleTokenVerifier.verify(request.getIdToken());

        User user = userRepository.findByGoogleId(googleUser.googleId())
                .or(() -> userRepository.findByEmail(googleUser.email()))
                .orElseGet(() -> createGoogleUser(googleUser));

        if (user.getGoogleId() == null) {
            user.setGoogleId(googleUser.googleId());
            user.setAuthProvider(AuthProvider.GOOGLE);
            user = userRepository.save(user);
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return buildAuthResponse(user, token);
    }

    private User createGoogleUser(GoogleUserInfo googleUser) {
        User user = User.builder()
                .name(googleUser.name())
                .email(googleUser.email())
                .password(null)
                .authProvider(AuthProvider.GOOGLE)
                .googleId(googleUser.googleId())
                .role(UserRole.CUSTOMER)
                .build();
        return userRepository.save(user);
    }

    private AuthResponse buildAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .token(token)
                .tokenType("Bearer")
                .build();
    }
}
