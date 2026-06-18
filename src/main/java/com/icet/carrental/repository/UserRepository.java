package com.icet.carrental.repository;

import com.icet.carrental.enums.AuthProvider;
import com.icet.carrental.enums.UserRole;
import com.icet.carrental.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<User> USER_ROW_MAPPER = (rs, rowNum) -> User.builder()
            .id(rs.getLong("id"))
            .name(rs.getString("name"))
            .email(rs.getString("email"))
            .password(rs.getString("password"))
            .authProvider(AuthProvider.valueOf(rs.getString("auth_provider")))
            .googleId(rs.getString("google_id"))
            .phone(rs.getString("phone"))
            .profilePictureUrl(rs.getString("profile_picture_url"))
            .role(UserRole.valueOf(rs.getString("role")))
            .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
            .updatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
            .build();

    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        return jdbcTemplate.query(sql, USER_ROW_MAPPER, id).stream().findFirst();
    }

    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        return jdbcTemplate.query(sql, USER_ROW_MAPPER, email).stream().findFirst();
    }

    public Optional<User> findByGoogleId(String googleId) {
        String sql = "SELECT * FROM users WHERE google_id = ?";
        return jdbcTemplate.query(sql, USER_ROW_MAPPER, googleId).stream().findFirst();
    }

    public List<User> findAll() {
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, USER_ROW_MAPPER);
    }

    public boolean existsByEmail(String email) {
        String  sql   = "SELECT COUNT(*) FROM users WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    public User save(User user) {
        if (user.getId() == null) {
            return insert(user);
        }
        return update(user);
    }

    private User insert(User user) {
        String sql = """
                INSERT INTO users (name, email, password, auth_provider, google_id, phone,
                                   profile_picture_url, role)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        AuthProvider authProvider = user.getAuthProvider() != null
                ? user.getAuthProvider() : AuthProvider.LOCAL;

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setString(4, authProvider.name());
            ps.setString(5, user.getGoogleId());
            ps.setString(6, user.getPhone());
            ps.setString(7, user.getProfilePictureUrl());
            ps.setString(8, user.getRole().name());
            return ps;
        }, keyHolder);

        user.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        user.setAuthProvider(authProvider);
        return user;
    }

    private User update(User user) {
        String sql = """
                UPDATE users
                SET name = ?, email = ?, phone = ?, profile_picture_url = ?, role = ?,
                    auth_provider = ?, google_id = ?, updated_at = NOW()
                WHERE id = ?
                """;
        AuthProvider authProvider = user.getAuthProvider() != null
                ? user.getAuthProvider() : AuthProvider.LOCAL;

        jdbcTemplate.update(sql,
                user.getName(), user.getEmail(), user.getPhone(), user.getProfilePictureUrl(),
                user.getRole().name(), authProvider.name(), user.getGoogleId(),
                user.getId());
        user.setAuthProvider(authProvider);
        return user;
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
