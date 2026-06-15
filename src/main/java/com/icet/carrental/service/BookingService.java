package com.icet.carrental.service;

import com.icet.carrental.dto.request.BookingRequest;
import com.icet.carrental.dto.response.BookingResponse;
import com.icet.carrental.enums.BookingStatus;
import com.icet.carrental.enums.CarStatus;
import com.icet.carrental.exception.BookingConflictException;
import com.icet.carrental.exception.CarNotAvailableException;
import com.icet.carrental.exception.ResourceNotFoundException;
import com.icet.carrental.exception.UnauthorizedException;
import com.icet.carrental.model.Booking;
import com.icet.carrental.model.Car;
import com.icet.carrental.model.User;
import com.icet.carrental.repository.BookingRepository;
import com.icet.carrental.repository.CarRepository;
import com.icet.carrental.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final CarRepository     carRepository;
    private final UserRepository    userRepository;

    @Transactional(readOnly = true)
    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(this::toBookingResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getMyBookings(String email) {
        User user = findUserByEmailOrThrow(email);
        return bookingRepository.findByUserId(user.getId()).stream()
                .map(this::toBookingResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long id, String email, boolean isAdmin) {
        Booking booking = findBookingOrThrow(id);

        if (!isAdmin) {
            User user = findUserByEmailOrThrow(email);
            if (!booking.getUserId().equals(user.getId())) {
                throw new UnauthorizedException("You are not allowed to view this booking");
            }
        }

        return toBookingResponse(booking);
    }

    @Transactional
    public BookingResponse createBooking(BookingRequest request, String email) {
        if (!request.getEndDate().isAfter(request.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        User user = findUserByEmailOrThrow(email);
        Car  car  = findCarOrThrow(request.getCarId());

        if (car.getStatus() != CarStatus.AVAILABLE) {
            throw new CarNotAvailableException(car.getId());
        }

        boolean available = bookingRepository.isCarAvailable(
                car.getId(), request.getStartDate(), request.getEndDate());

        if (!available) {
            throw new BookingConflictException(
                    "Car is already booked for the selected period");
        }

        long       days        = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate());
        BigDecimal totalAmount = car.getDailyRate().multiply(BigDecimal.valueOf(days));

        Booking booking = Booking.builder()
                .userId(user.getId())
                .carId(car.getId())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalAmount(totalAmount)
                .status(BookingStatus.PENDING)
                .build();

        return toBookingResponse(bookingRepository.save(booking));
    }

    @Transactional
    public BookingResponse updateBooking(Long id, BookingRequest request, String email) {
        Booking booking = findBookingOrThrow(id);
        User    user    = findUserByEmailOrThrow(email);

        if (!booking.getUserId().equals(user.getId())) {
            throw new UnauthorizedException("You are not allowed to modify this booking");
        }
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalArgumentException("Only PENDING bookings can be updated");
        }
        if (!request.getEndDate().isAfter(request.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        bookingRepository.updateDates(id, request.getStartDate(), request.getEndDate());
        return toBookingResponse(findBookingOrThrow(id));
    }

    @Transactional
    public void cancelBooking(Long id, String email) {
        Booking booking = findBookingOrThrow(id);
        User    user    = findUserByEmailOrThrow(email);

        if (!booking.getUserId().equals(user.getId())) {
            throw new UnauthorizedException("You are not allowed to cancel this booking");
        }
        if (booking.getStatus() == BookingStatus.COMPLETED
                || booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalArgumentException(
                    "Cannot cancel a booking with status: " + booking.getStatus());
        }

        bookingRepository.updateStatus(id, BookingStatus.CANCELLED);
    }

    @Transactional
    public BookingResponse updateBookingStatus(Long id, BookingStatus status) {
        findBookingOrThrow(id);
        bookingRepository.updateStatus(id, status);

        if (status == BookingStatus.APPROVED) {
            Booking booking = findBookingOrThrow(id);
            carRepository.updateStatus(booking.getCarId(), CarStatus.RENTED);
        }
        if (status == BookingStatus.COMPLETED || status == BookingStatus.REJECTED
                || status == BookingStatus.CANCELLED) {
            Booking booking = findBookingOrThrow(id);
            carRepository.updateStatus(booking.getCarId(), CarStatus.AVAILABLE);
        }

        return toBookingResponse(findBookingOrThrow(id));
    }

    private Booking findBookingOrThrow(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", id));
    }

    private User findUserByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private Car findCarOrThrow(Long id) {
        return carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Car", id));
    }

    private BookingResponse toBookingResponse(Booking booking) {
        User user = userRepository.findById(booking.getUserId()).orElse(null);
        Car  car  = carRepository.findById(booking.getCarId()).orElse(null);

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
    }
}
