package com.icet.carrental.service;

import com.icet.carrental.dto.request.RegisterRequest;
import com.icet.carrental.dto.response.UserResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {

    List<UserResponse> getAllUsers();

    UserResponse getUserById(Long id);

    UserResponse getMyProfile(String email);

    UserResponse updateUser(Long id, RegisterRequest request);

    UserResponse updateMyProfile(String email, RegisterRequest request);

    UserResponse uploadProfilePicture(String email, MultipartFile file);

    void deleteUser(Long id);
}
