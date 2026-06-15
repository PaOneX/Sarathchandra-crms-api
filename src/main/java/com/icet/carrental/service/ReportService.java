package com.icet.carrental.service;

import com.icet.carrental.dto.response.BookingResponse;
import com.icet.carrental.repository.BookingRepository;
import com.icet.carrental.repository.CarRepository;
import com.icet.carrental.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final BookingRepository bookingRepository;
    private final UserRepository    userRepository;
    private final CarRepository     carRepository;
    private final JdbcTemplate      jdbcTemplate;

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingReport(LocalDate startDate, LocalDate endDate) {
        return bookingRepository.findByDateRange(startDate, endDate).stream()
                .map(booking -> {
                    var user = userRepository.findById(booking.getUserId()).orElse(null);
                    var car  = carRepository.findById(booking.getCarId()).orElse(null);
                    return BookingResponse.builder()
                            .id(booking.getId())
                            .userId(booking.getUserId())
                            .customerName(user != null ? user.getName() : "Unknown")
                            .carId(booking.getCarId())
                            .carBrand(car != null ? car.getBrand() : "Unknown")
                            .carModel(car != null ? car.getModel() : "Unknown")
                            .startDate(booking.getStartDate())
                            .endDate(booking.getEndDate())
                            .totalAmount(booking.getTotalAmount())
                            .status(booking.getStatus())
                            .createdAt(booking.getCreatedAt())
                            .build();
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getRevenueReport(String period) {
        String groupBy = switch (period.toUpperCase()) {
            case "WEEKLY"  -> "YEARWEEK(b.start_date)";
            case "MONTHLY" -> "DATE_FORMAT(b.start_date, '%Y-%m')";
            default        -> "DATE(b.start_date)";
        };

        String sql = """
                SELECT %s AS period, SUM(p.amount) AS revenue, COUNT(p.id) AS transactions
                FROM payments p
                INNER JOIN bookings b ON p.booking_id = b.id
                WHERE p.status = 'COMPLETED'
                GROUP BY %s
                ORDER BY period DESC
                """.formatted(groupBy, groupBy);

        return jdbcTemplate.queryForList(sql);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getCustomerReport() {
        String sql = """
                SELECT u.id, u.name, u.email,
                       COUNT(b.id)         AS total_bookings,
                       SUM(p.amount)       AS total_spent,
                       MAX(b.created_at)   AS last_booking
                FROM users u
                LEFT JOIN bookings b ON u.id = b.user_id
                LEFT JOIN payments p ON b.id = p.booking_id AND p.status = 'COMPLETED'
                WHERE u.role = 'CUSTOMER'
                GROUP BY u.id, u.name, u.email
                ORDER BY total_bookings DESC
                """;
        return jdbcTemplate.queryForList(sql);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getCarUtilizationReport() {
        String sql = """
                SELECT c.id, c.brand, c.model, c.status,
                       COUNT(b.id)       AS total_bookings,
                       SUM(p.amount)     AS total_revenue,
                       COALESCE(SUM(DATEDIFF(b.end_date, b.start_date)), 0) AS total_days_rented
                FROM cars c
                LEFT JOIN bookings b ON c.id = b.car_id
                    AND b.status NOT IN ('REJECTED', 'CANCELLED')
                LEFT JOIN payments p ON b.id = p.booking_id AND p.status = 'COMPLETED'
                GROUP BY c.id, c.brand, c.model, c.status
                ORDER BY total_bookings DESC
                """;
        return jdbcTemplate.queryForList(sql);
    }
}
