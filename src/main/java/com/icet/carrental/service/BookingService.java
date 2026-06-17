package com.icet.carrental.service;

import com.icet.carrental.dto.request.BookingRequest;
import com.icet.carrental.dto.response.BookingResponse;
import com.icet.carrental.enums.BookingStatus;

import java.util.List;

public interface BookingService {

    List<BookingResponse> getAllBookings();

    List<BookingResponse> getMyBookings(String email);

    BookingResponse getBookingById(Long id, String email, boolean isAdmin);

    BookingResponse createBooking(BookingRequest request, String email);

    BookingResponse updateBooking(Long id, BookingRequest request, String email);

    void cancelBooking(Long id, String email);

    BookingResponse updateBookingStatus(Long id, BookingStatus status);
}
