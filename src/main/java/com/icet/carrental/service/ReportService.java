package com.icet.carrental.service;

import com.icet.carrental.dto.response.BookingResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ReportService {

    List<BookingResponse> getBookingReport(LocalDate startDate, LocalDate endDate);

    List<Map<String, Object>> getRevenueReport(String period);

    List<Map<String, Object>> getCustomerReport();

    List<Map<String, Object>> getCarUtilizationReport();
}
