package com.icet.carrental.service.impl;

import com.icet.carrental.dto.response.CarResponse;
import com.icet.carrental.enums.CarStatus;
import com.icet.carrental.enums.FuelType;
import com.icet.carrental.model.Car;
import com.icet.carrental.repository.CarRepository;
import com.icet.carrental.service.CarService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FleetServiceImplTest {

    @Mock private CarRepository carRepository;
    @Mock private CarService    carService;

    @InjectMocks
    private FleetServiceImpl fleetService;

    @Test
    void getAvailableCars_returnsMappedResponses() {
        Car car = sampleCar(1L, CarStatus.AVAILABLE);
        CarResponse response = CarResponse.builder().id(1L).brand("Toyota").build();

        when(carRepository.findByStatus(CarStatus.AVAILABLE)).thenReturn(List.of(car));
        when(carService.toCarResponse(car)).thenReturn(response);

        List<CarResponse> results = fleetService.getAvailableCars();

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getBrand()).isEqualTo("Toyota");
    }

    @Test
    void updateCarStatus_updatesAndReturnsResponse() {
        Car car = sampleCar(5L, CarStatus.UNDER_MAINTENANCE);
        CarResponse response = CarResponse.builder().id(5L).status(CarStatus.UNDER_MAINTENANCE).build();

        when(carRepository.findById(5L)).thenReturn(Optional.of(car));
        when(carService.toCarResponse(car)).thenReturn(response);

        CarResponse result = fleetService.updateCarStatus(5L, CarStatus.UNDER_MAINTENANCE);

        verify(carRepository).updateStatus(5L, CarStatus.UNDER_MAINTENANCE);
        assertThat(result.getStatus()).isEqualTo(CarStatus.UNDER_MAINTENANCE);
    }

    private Car sampleCar(Long id, CarStatus status) {
        return Car.builder()
                .id(id)
                .brand("Toyota")
                .model("Camry")
                .fuelType(FuelType.PETROL)
                .seatingCapacity(5)
                .dailyRate(BigDecimal.valueOf(50))
                .status(status)
                .year(2024)
                .licensePlate("ABC-123")
                .createdAt(LocalDateTime.now())
                .build();
    }
}
