package com.icet.carrental.service;

import com.icet.carrental.dto.request.CarRequest;
import com.icet.carrental.dto.response.CarResponse;
import com.icet.carrental.enums.CarStatus;
import com.icet.carrental.model.Car;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CarService {

    List<CarResponse> getAllCars(String brand, String fuelType, Double minPrice, Double maxPrice);

    CarResponse getCarById(Long id);

    CarResponse addCar(CarRequest request);

    CarResponse updateCar(Long id, CarRequest request);

    void updateCarStatus(Long id, CarStatus status);

    void deleteCar(Long id);

    CarResponse uploadCarImages(Long carId, MultipartFile[] files);

    void deleteCarImage(Long carId, Long imageId);

    CarResponse toCarResponse(Car car);
}
