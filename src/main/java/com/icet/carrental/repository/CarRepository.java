package com.icet.carrental.repository;

import com.icet.carrental.enums.CarStatus;
import com.icet.carrental.enums.FuelType;
import com.icet.carrental.model.Car;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CarRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<Car> CAR_ROW_MAPPER = (rs, rowNum) -> Car.builder()
            .id(rs.getLong("id"))
            .brand(rs.getString("brand"))
            .model(rs.getString("model"))
            .fuelType(FuelType.valueOf(rs.getString("fuel_type")))
            .seatingCapacity(rs.getInt("seating_capacity"))
            .dailyRate(rs.getBigDecimal("daily_rate"))
            .status(CarStatus.valueOf(rs.getString("status")))
            .year(rs.getInt("year"))
            .licensePlate(rs.getString("license_plate"))
            .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
            .updatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
            .build();

    public Optional<Car> findById(Long id) {
        String sql = "SELECT * FROM cars WHERE id = ?";
        return jdbcTemplate.query(sql, CAR_ROW_MAPPER, id).stream().findFirst();
    }

    public List<Car> findAll() {
        String sql = "SELECT * FROM cars ORDER BY brand, model";
        return jdbcTemplate.query(sql, CAR_ROW_MAPPER);
    }

    public List<Car> findWithFilters(String brand, String fuelType, Double minPrice, Double maxPrice) {
        StringBuilder sql    = new StringBuilder("SELECT * FROM cars WHERE 1=1");
        List<Object>  params = new ArrayList<>();

        if (brand != null && !brand.isBlank()) {
            sql.append(" AND brand LIKE ?");
            params.add("%" + brand + "%");
        }
        if (fuelType != null && !fuelType.isBlank()) {
            sql.append(" AND fuel_type = ?");
            params.add(fuelType.toUpperCase());
        }
        if (minPrice != null) {
            sql.append(" AND daily_rate >= ?");
            params.add(minPrice);
        }
        if (maxPrice != null) {
            sql.append(" AND daily_rate <= ?");
            params.add(maxPrice);
        }

        sql.append(" ORDER BY brand, model");
        return jdbcTemplate.query(sql.toString(), CAR_ROW_MAPPER, params.toArray());
    }

    public List<Car> findByStatus(CarStatus status) {
        String sql = "SELECT * FROM cars WHERE status = ?";
        return jdbcTemplate.query(sql, CAR_ROW_MAPPER, status.name());
    }

    public Car save(Car car) {
        if (car.getId() == null) {
            return insert(car);
        }
        return update(car);
    }

    private Car insert(Car car) {
        String sql = """
                INSERT INTO cars (brand, model, fuel_type, seating_capacity, daily_rate,
                                  status, year, license_plate)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, car.getBrand());
            ps.setString(2, car.getModel());
            ps.setString(3, car.getFuelType().name());
            ps.setInt(4, car.getSeatingCapacity());
            ps.setBigDecimal(5, car.getDailyRate());
            ps.setString(6, car.getStatus() != null ? car.getStatus().name() : CarStatus.AVAILABLE.name());
            ps.setObject(7, car.getYear());
            ps.setString(8, car.getLicensePlate());
            return ps;
        }, keyHolder);

        car.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return car;
    }

    private Car update(Car car) {
        String sql = """
                UPDATE cars
                SET brand = ?, model = ?, fuel_type = ?, seating_capacity = ?,
                    daily_rate = ?, year = ?, license_plate = ?, updated_at = NOW()
                WHERE id = ?
                """;
        jdbcTemplate.update(sql,
                car.getBrand(), car.getModel(), car.getFuelType().name(),
                car.getSeatingCapacity(), car.getDailyRate(),
                car.getYear(), car.getLicensePlate(), car.getId());
        return car;
    }

    public void updateStatus(Long id, CarStatus status) {
        String sql = "UPDATE cars SET status = ?, updated_at = NOW() WHERE id = ?";
        jdbcTemplate.update(sql, status.name(), id);
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM cars WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
