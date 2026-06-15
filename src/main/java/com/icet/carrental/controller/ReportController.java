package com.icet.carrental.controller;

import com.icet.carrental.dto.response.ApiResponse;
import com.icet.carrental.dto.response.BookingResponse;
import com.icet.carrental.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/bookings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getBookingReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return ResponseEntity.ok(
                ApiResponse.success(reportService.getBookingReport(startDate, endDate)));
    }

    @GetMapping("/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getRevenueReport(
            @RequestParam(defaultValue = "MONTHLY") String period) {

        return ResponseEntity.ok(
                ApiResponse.success(reportService.getRevenueReport(period)));
    }

    @GetMapping("/customers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getCustomerReport() {
        return ResponseEntity.ok(ApiResponse.success(reportService.getCustomerReport()));
    }

    @GetMapping("/car-utilization")
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getCarUtilizationReport() {
        return ResponseEntity.ok(ApiResponse.success(reportService.getCarUtilizationReport()));
    }
}
