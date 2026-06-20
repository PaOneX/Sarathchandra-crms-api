package com.icet.carrental.config;

import com.icet.carrental.enums.AuthProvider;
import com.icet.carrental.enums.UserRole;
import com.icet.carrental.model.User;
import com.icet.carrental.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminUserSeeder implements ApplicationRunner {

    private static final int VALID_BCRYPT_HASH_LENGTH = 60;

    private final AdminSeedProperties adminSeedProperties;
    private final UserRepository      userRepository;
    private final PasswordEncoder     passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (!adminSeedProperties.isEnabled()) {
            return;
        }

        String email    = adminSeedProperties.getEmail();
        String password = adminSeedProperties.getPassword();

        if (!StringUtils.hasText(email) || !StringUtils.hasText(password)) {
            log.warn("Admin seeder enabled but ADMIN_EMAIL or ADMIN_PASSWORD is missing; skipping");
            return;
        }

        userRepository.findByEmail(email).ifPresentOrElse(
                user -> repairExistingAdmin(user, password),
                () -> createAdmin(email, password)
        );
    }

    private void createAdmin(String email, String password) {
        User admin = User.builder()
                .name(adminSeedProperties.getName())
                .email(email)
                .password(passwordEncoder.encode(password))
                .authProvider(AuthProvider.LOCAL)
                .role(UserRole.ADMIN)
                .build();

        userRepository.save(admin);
        log.info("Created default admin account for {}", email);
    }

    private void repairExistingAdmin(User user, String password) {
        boolean roleUpdated     = user.getRole() != UserRole.ADMIN;
        boolean passwordInvalid = isPasswordHashInvalid(user.getPassword())
                || !passwordEncoder.matches(password, user.getPassword());

        if (roleUpdated) {
            user.setRole(UserRole.ADMIN);
        }

        if (passwordInvalid) {
            userRepository.updatePassword(user.getId(), passwordEncoder.encode(password));
            log.info("Reset admin password for {}", user.getEmail());
        }

        if (roleUpdated) {
            userRepository.save(user);
            log.info("Promoted {} to ADMIN", user.getEmail());
        }
    }

    private boolean isPasswordHashInvalid(String encodedPassword) {
        return encodedPassword == null || encodedPassword.length() < VALID_BCRYPT_HASH_LENGTH;
    }
}
