package com.icet.carrental.repository;

import com.icet.carrental.model.CarImage;
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
public class CarImageRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<CarImage> ROW_MAPPER = (rs, rowNum) -> CarImage.builder()
            .id(rs.getLong("id"))
            .carId(rs.getLong("car_id"))
            .storagePath(rs.getString("storage_path"))
            .url(rs.getString("url"))
            .sortOrder(rs.getInt("sort_order"))
            .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
            .build();

    public List<CarImage> findByCarId(Long carId) {
        String sql = "SELECT * FROM car_images WHERE car_id = ? ORDER BY sort_order, id";
        return jdbcTemplate.query(sql, ROW_MAPPER, carId);
    }

    public Optional<CarImage> findById(Long id) {
        String sql = "SELECT * FROM car_images WHERE id = ?";
        return jdbcTemplate.query(sql, ROW_MAPPER, id).stream().findFirst();
    }

    public int countByCarId(Long carId) {
        String  sql   = "SELECT COUNT(*) FROM car_images WHERE car_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, carId);
        return count != null ? count : 0;
    }

    public CarImage save(CarImage image) {
        if (image.getId() == null) {
            return insert(image);
        }
        return update(image);
    }

    private CarImage insert(CarImage image) {
        String sql = """
                INSERT INTO car_images (car_id, storage_path, url, sort_order)
                VALUES (?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, image.getCarId());
            ps.setString(2, image.getStoragePath());
            ps.setString(3, image.getUrl());
            ps.setInt(4, image.getSortOrder());
            return ps;
        }, keyHolder);

        image.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return image;
    }

    private CarImage update(CarImage image) {
        String sql = """
                UPDATE car_images
                SET storage_path = ?, url = ?, sort_order = ?
                WHERE id = ?
                """;
        jdbcTemplate.update(sql,
                image.getStoragePath(), image.getUrl(), image.getSortOrder(), image.getId());
        return image;
    }

    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM car_images WHERE id = ?", id);
    }
}
