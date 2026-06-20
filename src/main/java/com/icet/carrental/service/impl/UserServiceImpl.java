package com.icet.carrental.service.impl;

import com.icet.carrental.config.SupabaseProperties;
import com.icet.carrental.dto.request.RegisterRequest;
import com.icet.carrental.dto.request.UpdateProfileRequest;
import com.icet.carrental.dto.response.UserResponse;
import com.icet.carrental.exception.ResourceNotFoundException;
import com.icet.carrental.model.User;
import com.icet.carrental.repository.UserRepository;
import com.icet.carrental.service.UserService;
import com.icet.carrental.service.storage.StorageService;
import com.icet.carrental.util.ImageFileValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository     userRepository;
    private final StorageService     storageService;
    private final SupabaseProperties supabaseProperties;

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return toUserResponse(findUserOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getMyProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        return toUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, RegisterRequest request) {
        User user = findUserOrThrow(id);
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        return toUserResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse updateMyProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        user.setName(request.getName());
        user.setPhone(request.getPhone());
        return toUserResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse uploadProfilePicture(String email, MultipartFile file) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        ImageFileValidator.validate(file);

        String bucket      = supabaseProperties.getStorage().getAvatars();
        String extension   = ImageFileValidator.resolveExtension(file);
        String path        = ImageFileValidator.generateObjectName("avatars/" + user.getId(), extension);
        String contentType = ImageFileValidator.normalizeContentType(file.getContentType());

        deleteExistingAvatar(bucket, user.getProfilePictureUrl());

        String publicUrl = storageService.upload(
                bucket, path, ImageFileValidator.readBytes(file), contentType);

        user.setProfilePictureUrl(publicUrl);
        return toUserResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        findUserOrThrow(id);
        userRepository.deleteById(id);
    }

    private void deleteExistingAvatar(String bucket, String profilePictureUrl) {
        if (profilePictureUrl == null || profilePictureUrl.isBlank()) {
            return;
        }

        String storagePath = extractStoragePath(bucket, profilePictureUrl);
        if (storagePath == null) {
            return;
        }

        try {
            storageService.delete(bucket, storagePath);
        } catch (Exception ex) {
            log.warn("Failed to delete previous avatar at {}: {}", storagePath, ex.getMessage());
        }
    }

    private String extractStoragePath(String bucket, String publicUrl) {
        String marker = "/storage/v1/object/public/" + bucket + "/";
        int    index  = publicUrl.indexOf(marker);
        if (index < 0) {
            return null;
        }
        return publicUrl.substring(index + marker.length());
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
                .profilePictureUrl(user.getProfilePictureUrl())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
