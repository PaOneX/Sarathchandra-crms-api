package com.icet.carrental.service;

import com.icet.carrental.dto.request.RegisterRequest;
import com.icet.carrental.dto.response.UserResponse;
import com.icet.carrental.exception.ResourceNotFoundException;
import com.icet.carrental.model.User;
import com.icet.carrental.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return toUserResponse(findUserOrThrow(id));
    }

    @Transactional(readOnly = true)
    public UserResponse getMyProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        return toUserResponse(user);
    }

    @Transactional
    public UserResponse updateUser(Long id, RegisterRequest request) {
        User user = findUserOrThrow(id);
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        return toUserResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateMyProfile(String email, RegisterRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        user.setName(request.getName());
        user.setPhone(request.getPhone());
        return toUserResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        findUserOrThrow(id);
        userRepository.deleteById(id);
    }

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
