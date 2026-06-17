package com.icet.carrental.service.impl;

import com.icet.carrental.dto.request.CarRequest;
import com.icet.carrental.dto.response.CarResponse;
import com.icet.carrental.enums.CarStatus;
import com.icet.carrental.exception.ResourceNotFoundException;
import com.icet.carrental.model.Car;
import com.icet.carrental.repository.CarRepository;
import com.icet.carrental.service.CarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {

    private final CarRepository carRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CarResponse> getAllCars(String brand, String fuelType,
                                        Double minPrice, Double maxPrice) {
        return carRepository.findWithFilters(brand, fuelType, minPrice, maxPrice)
                .stream()
                .map(this::toCarResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CarResponse getCarById(Long id) {
        Car car = findCarOrThrow(id);
        return toCarResponse(car);
    }

    @Override
    @Transactional
    public CarResponse addCar(CarRequest request) {
        Car car = Car.builder()
                .brand(request.getBrand())
                .model(request.getModel())
                .fuelType(request.getFuelType())
                .seatingCapacity(request.getSeatingCapacity())
                .dailyRate(request.getDailyRate())
                .status(CarStatus.AVAILABLE)
                .year(request.getYear())
                .licensePlate(request.getLicensePlate())
                .build();

        return toCarResponse(carRepository.save(car));
    }

    @Override
    @Transactional
    public CarResponse updateCar(Long id, CarRequest request) {
        Car car = findCarOrThrow(id);

        car.setBrand(request.getBrand());
        car.setModel(request.getModel());
        car.setFuelType(request.getFuelType());
        car.setSeatingCapacity(request.getSeatingCapacity());
        car.setDailyRate(request.getDailyRate());
        car.setYear(request.getYear());
        car.setLicensePlate(request.getLicensePlate());

        return toCarResponse(carRepository.save(car));
    }

    @Override
    @Transactional
    public void updateCarStatus(Long id, CarStatus status) {
        findCarOrThrow(id);
        carRepository.updateStatus(id, status);
    }

    @Override
    @Transactional
    public void deleteCar(Long id) {
        findCarOrThrow(id);
        carRepository.deleteById(id);
    }

    private Car findCarOrThrow(Long id) {
        return carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Car", id));
    }

    @Override
    public CarResponse toCarResponse(Car car) {
        return CarResponse.builder()
                .id(car.getId())
                .brand(car.getBrand())
                .model(car.getModel())
                .fuelType(car.getFuelType())
                .seatingCapacity(car.getSeatingCapacity())
                .dailyRate(car.getDailyRate())
                .status(car.getStatus())
                .year(car.getYear())
                .licensePlate(car.getLicensePlate())
                .createdAt(car.getCreatedAt())
                .build();
    }
}
