package com.icet.carrental.service.impl;

import com.icet.carrental.config.WhatsAppProperties;
import com.icet.carrental.dto.request.BookingRequest;
import com.icet.carrental.dto.response.BookingResponse;
import com.icet.carrental.enums.BookingStatus;
import com.icet.carrental.enums.CarStatus;
import com.icet.carrental.enums.FuelType;
import com.icet.carrental.exception.BookingConflictException;
import com.icet.carrental.model.Booking;
import com.icet.carrental.model.Car;
import com.icet.carrental.model.User;
import com.icet.carrental.repository.BookingRepository;
import com.icet.carrental.repository.CarRepository;
import com.icet.carrental.repository.UserRepository;
import com.icet.carrental.service.WhatsAppService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private CarRepository     carRepository;
    @Mock private UserRepository    userRepository;
    @Mock private WhatsAppService   whatsAppService;
    @Mock private WhatsAppProperties whatsAppProperties;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Test
    void updateBooking_recalculatesTotalAndChecksAvailability() {
        BookingRequest request = new BookingRequest();
        request.setCarId(1L);
        request.setStartDate(LocalDate.of(2026, 7, 1));
        request.setEndDate(LocalDate.of(2026, 7, 4));

        Booking booking = Booking.builder()
                .id(10L)
                .userId(1L)
                .carId(1L)
                .startDate(LocalDate.of(2026, 6, 1))
                .endDate(LocalDate.of(2026, 6, 3))
                .totalAmount(BigDecimal.valueOf(90))
                .status(BookingStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        User user = User.builder().id(1L).email("user@example.com").build();
        Car car = Car.builder()
                .id(1L)
                .brand("Toyota")
                .model("Camry")
                .dailyRate(BigDecimal.valueOf(50))
                .status(CarStatus.AVAILABLE)
                .fuelType(FuelType.PETROL)
                .build();

        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        when(bookingRepository.isCarAvailable(1L, request.getStartDate(), request.getEndDate(), 10L))
                .thenReturn(true);
        when(whatsAppProperties.getAdvancePercent()).thenReturn(30);

        Booking updated = Booking.builder()
                .id(10L)
                .userId(1L)
                .carId(1L)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalAmount(BigDecimal.valueOf(150))
                .status(BookingStatus.PENDING)
                .createdAt(booking.getCreatedAt())
                .build();
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking), Optional.of(updated));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        BookingResponse response = bookingService.updateBooking(10L, request, "user@example.com");

        verify(bookingRepository).updateDatesAndAmount(
                eq(10L),
                eq(request.getStartDate()),
                eq(request.getEndDate()),
                eq(BigDecimal.valueOf(150))
        );
        assertThat(response.getTotalAmount()).isEqualByComparingTo("150");
    }

    @Test
    void updateBooking_throwsWhenDatesConflict() {
        BookingRequest request = new BookingRequest();
        request.setCarId(1L);
        request.setStartDate(LocalDate.of(2026, 7, 1));
        request.setEndDate(LocalDate.of(2026, 7, 4));

        Booking booking = Booking.builder()
                .id(10L)
                .userId(1L)
                .carId(1L)
                .status(BookingStatus.PENDING)
                .build();

        User user = User.builder().id(1L).email("user@example.com").build();
        Car car = Car.builder().id(1L).dailyRate(BigDecimal.TEN).fuelType(FuelType.PETROL).build();

        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        when(bookingRepository.isCarAvailable(1L, request.getStartDate(), request.getEndDate(), 10L))
                .thenReturn(false);

        assertThatThrownBy(() -> bookingService.updateBooking(10L, request, "user@example.com"))
                .isInstanceOf(BookingConflictException.class);
    }

    @Test
    void updateBookingStatus_rejectsInvalidTransition() {
        Booking booking = Booking.builder()
                .id(5L)
                .userId(1L)
                .carId(2L)
                .status(BookingStatus.COMPLETED)
                .build();

        when(bookingRepository.findById(5L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.updateBookingStatus(5L, BookingStatus.APPROVED))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot transition booking");
    }

    @Test
    void updateBookingStatus_approvesPendingBookingAndMarksCarRented() {
        Booking booking = Booking.builder()
                .id(5L)
                .userId(1L)
                .carId(2L)
                .status(BookingStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(100))
                .createdAt(LocalDateTime.now())
                .build();

        Car car = Car.builder()
                .id(2L)
                .brand("BMW")
                .model("X5")
                .status(CarStatus.AVAILABLE)
                .fuelType(FuelType.DIESEL)
                .build();

        Booking approved = Booking.builder()
                .id(5L)
                .userId(1L)
                .carId(2L)
                .status(BookingStatus.APPROVED)
                .totalAmount(BigDecimal.valueOf(100))
                .createdAt(LocalDateTime.now())
                .build();

        when(bookingRepository.findById(5L)).thenReturn(
                Optional.of(booking),
                Optional.of(approved)
        );
        when(carRepository.findById(2L)).thenReturn(Optional.of(car));
        when(userRepository.findById(1L)).thenReturn(Optional.of(User.builder().id(1L).name("User").build()));
        when(whatsAppProperties.getAdvancePercent()).thenReturn(30);

        bookingService.updateBookingStatus(5L, BookingStatus.APPROVED);

        verify(bookingRepository).updateStatus(5L, BookingStatus.APPROVED);
        verify(carRepository).updateStatus(2L, CarStatus.RENTED);
    }
}
