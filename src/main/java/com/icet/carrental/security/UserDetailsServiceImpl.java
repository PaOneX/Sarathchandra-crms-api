package com.icet.carrental.security;

import com.icet.carrental.enums.AuthProvider;
import com.icet.carrental.model.User;
import com.icet.carrental.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    static final String GOOGLE_PASSWORD_PLACEHOLDER = "{noop}GOOGLE_OAUTH_USER";

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + email));

        String password = resolvePassword(user);

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(password)
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
                .build();
    }

    private String resolvePassword(User user) {
        if (user.getPassword() != null) {
            return user.getPassword();
        }
        if (user.getAuthProvider() == AuthProvider.GOOGLE) {
            return GOOGLE_PASSWORD_PLACEHOLDER;
        }
        return GOOGLE_PASSWORD_PLACEHOLDER;
    }
}
