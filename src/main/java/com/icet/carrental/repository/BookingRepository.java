package com.icet.carrental.repository;

import com.icet.carrental.enums.BookingStatus;
import com.icet.carrental.model.Booking;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BookingRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<Booking> BOOKING_ROW_MAPPER = (rs, rowNum) -> Booking.builder()
            .id(rs.getLong("id"))
            .userId(rs.getLong("user_id"))
            .carId(rs.getLong("car_id"))
            .startDate(rs.getDate("start_date").toLocalDate())
            .endDate(rs.getDate("end_date").toLocalDate())
            .totalAmount(rs.getBigDecimal("total_amount"))
            .status(BookingStatus.valueOf(rs.getString("status")))
            .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
            .updatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
            .build();

    public Optional<Booking> findById(Long id) {
        String sql = "SELECT * FROM bookings WHERE id = ?";
        return jdbcTemplate.query(sql, BOOKING_ROW_MAPPER, id).stream().findFirst();
    }

    public List<Booking> findAll() {
        String sql = "SELECT * FROM bookings ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, BOOKING_ROW_MAPPER);
    }

    public List<Booking> findByUserId(Long userId) {
        String sql = "SELECT * FROM bookings WHERE user_id = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, BOOKING_ROW_MAPPER, userId);
    }

    public boolean isCarAvailable(Long carId, LocalDate startDate, LocalDate endDate) {
        String sql = """
                SELECT COUNT(*) FROM bookings
                WHERE car_id = ?
                  AND status NOT IN ('REJECTED', 'CANCELLED')
                  AND start_date < ?
                  AND end_date > ?
                """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class,
                carId, Date.valueOf(endDate), Date.valueOf(startDate));
        return count != null && count == 0;
    }

    public List<Booking> findByDateRange(LocalDate startDate, LocalDate endDate) {
        String sql = """
                SELECT * FROM bookings
                WHERE start_date >= ? AND end_date <= ?
                ORDER BY created_at DESC
                """;
        return jdbcTemplate.query(sql, BOOKING_ROW_MAPPER,
                Date.valueOf(startDate), Date.valueOf(endDate));
    }

    public Booking save(Booking booking) {
        String sql = """
                INSERT INTO bookings (user_id, car_id, start_date, end_date, total_amount, status)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, booking.getUserId());
            ps.setLong(2, booking.getCarId());
            ps.setDate(3, Date.valueOf(booking.getStartDate()));
            ps.setDate(4, Date.valueOf(booking.getEndDate()));
            ps.setBigDecimal(5, booking.getTotalAmount());
            ps.setString(6, booking.getStatus().name());
            return ps;
        }, keyHolder);

        booking.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return booking;
    }

    public void updateDates(Long id, LocalDate startDate, LocalDate endDate) {
        String sql = """
                UPDATE bookings
                SET start_date = ?, end_date = ?, updated_at = NOW()
                WHERE id = ?
                """;
        jdbcTemplate.update(sql, Date.valueOf(startDate), Date.valueOf(endDate), id);
    }

    public void updateStatus(Long id, BookingStatus status) {
        String sql = "UPDATE bookings SET status = ?, updated_at = NOW() WHERE id = ?";
        jdbcTemplate.update(sql, status.name(), id);
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM bookings WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
