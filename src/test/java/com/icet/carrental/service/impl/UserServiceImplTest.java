package com.icet.carrental.service.impl;

import com.icet.carrental.config.SupabaseProperties;
import com.icet.carrental.dto.response.UserResponse;
import com.icet.carrental.enums.UserRole;
import com.icet.carrental.model.User;
import com.icet.carrental.repository.UserRepository;
import com.icet.carrental.service.storage.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository     userRepository;
    @Mock private StorageService    storageService;
    @Mock private SupabaseProperties supabaseProperties;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        SupabaseProperties.StorageBuckets buckets = new SupabaseProperties.StorageBuckets();
        buckets.setAvatars("avatars");
        when(supabaseProperties.getStorage()).thenReturn(buckets);
    }

    @Test
    void uploadProfilePicture_uploadsAndUpdatesUser() {
        User user = User.builder()
                .id(1L)
                .name("Jane")
                .email("jane@example.com")
                .role(UserRole.CUSTOMER)
                .createdAt(LocalDateTime.now())
                .build();

        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.png", "image/png", "avatar-data".getBytes());

        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(user));
        when(storageService.upload(eq("avatars"), any(), any(), eq("image/png")))
                .thenReturn("https://test.supabase.co/storage/v1/object/public/avatars/avatars/1/uuid.png");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.uploadProfilePicture("jane@example.com", file);

        assertThat(response.getProfilePictureUrl())
                .contains("/storage/v1/object/public/avatars/");
        verify(userRepository).save(user);
        assertThat(user.getProfilePictureUrl()).contains("avatars/1");
    }
}
