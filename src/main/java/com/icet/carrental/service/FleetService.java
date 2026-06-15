package com.icet.carrental.service;

import com.icet.carrental.dto.response.CarResponse;
import com.icet.carrental.enums.CarStatus;
import com.icet.carrental.exception.ResourceNotFoundException;
import com.icet.carrental.repository.CarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FleetService {

    private final CarRepository carRepository;
    private final CarService    carService;

    @Transactional(readOnly = true)
    public List<CarResponse> getAvailableCars() {
        return carRepository.findByStatus(CarStatus.AVAILABLE).stream()
                .map(carService::toCarResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CarResponse> getCarsUnderMaintenance() {
        return carRepository.findByStatus(CarStatus.UNDER_MAINTENANCE).stream()
                .map(carService::toCarResponse)
                .toList();
    }

    @Transactional
    public CarResponse updateCarStatus(Long id, CarStatus status) {
        carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Car", id));
        carRepository.updateStatus(id, status);
        return carService.toCarResponse(
                carRepository.findById(id).orElseThrow());
    }
}
