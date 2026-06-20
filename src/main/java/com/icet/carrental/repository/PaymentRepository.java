package com.icet.carrental.repository;

import com.icet.carrental.enums.PaymentStatus;
import com.icet.carrental.model.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PaymentRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<Payment> PAYMENT_ROW_MAPPER = (rs, rowNum) -> {
        Timestamp paidAt = rs.getTimestamp("paid_at");
        return Payment.builder()
                .id(rs.getLong("id"))
                .bookingId(rs.getLong("booking_id"))
                .amount(rs.getBigDecimal("amount"))
                .paymentMethod(rs.getString("payment_method"))
                .transactionId(rs.getString("transaction_id"))
                .status(PaymentStatus.valueOf(rs.getString("status")))
                .paidAt(paidAt != null ? paidAt.toLocalDateTime() : null)
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .build();
    };

    public Optional<Payment> findById(Long id) {
        String sql = "SELECT * FROM payments WHERE id = ?";
        return jdbcTemplate.query(sql, PAYMENT_ROW_MAPPER, id).stream().findFirst();
    }

    public Optional<Payment> findByBookingId(Long bookingId) {
        String sql = "SELECT * FROM payments WHERE booking_id = ?";
        return jdbcTemplate.query(sql, PAYMENT_ROW_MAPPER, bookingId).stream().findFirst();
    }

    public List<Payment> findAll() {
        String sql = "SELECT * FROM payments ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, PAYMENT_ROW_MAPPER);
    }

    public List<Payment> findByUserId(Long userId) {
        String sql = """
                SELECT p.* FROM payments p
                INNER JOIN bookings b ON p.booking_id = b.id
                WHERE b.user_id = ?
                ORDER BY p.created_at DESC
                """;
        return jdbcTemplate.query(sql, PAYMENT_ROW_MAPPER, userId);
    }

    public Payment save(Payment payment) {
        String sql = """
                INSERT INTO payments (booking_id, amount, payment_method, transaction_id,
                                      status, paid_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, payment.getBookingId());
            ps.setBigDecimal(2, payment.getAmount());
            ps.setString(3, payment.getPaymentMethod());
            ps.setString(4, payment.getTransactionId());
            ps.setString(5, payment.getStatus().name());
            ps.setTimestamp(6, payment.getPaidAt() != null
                    ? Timestamp.valueOf(payment.getPaidAt()) : null);
            return ps;
        }, keyHolder);

        payment.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return payment;
    }

    public void updateTransactionId(Long id, String transactionId) {
        String sql = "UPDATE payments SET transaction_id = ? WHERE id = ?";
        jdbcTemplate.update(sql, transactionId, id);
    }

    public void updateStatus(Long id, PaymentStatus status, LocalDateTime paidAt) {
        String sql = "UPDATE payments SET status = ?, paid_at = ? WHERE id = ?";
        jdbcTemplate.update(sql, status.name(),
                paidAt != null ? Timestamp.valueOf(paidAt) : null, id);
    }
}
