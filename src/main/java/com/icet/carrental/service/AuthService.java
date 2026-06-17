package com.icet.carrental.service;

import com.icet.carrental.dto.request.GoogleAuthRequest;
import com.icet.carrental.dto.request.LoginRequest;
import com.icet.carrental.dto.request.RegisterRequest;
import com.icet.carrental.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse googleAuth(GoogleAuthRequest request);
}
