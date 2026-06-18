package com.icet.carrental.controller;

import com.icet.carrental.dto.request.CarRequest;
import com.icet.carrental.dto.request.UpdateCarStatusRequest;
import com.icet.carrental.dto.response.ApiResponse;
import com.icet.carrental.dto.response.CarResponse;
import com.icet.carrental.service.CarService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/cars")
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CarResponse>>> getAllCars(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String fuelType,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {

        List<CarResponse> cars = carService.getAllCars(brand, fuelType, minPrice, maxPrice);
        return ResponseEntity.ok(ApiResponse.success(cars));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CarResponse>> getCarById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(carService.getCarById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CarResponse>> addCar(
            @Valid @RequestBody CarRequest request) {

        CarResponse car = carService.addCar(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Car added successfully", car));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CarResponse>> updateCar(
            @PathVariable Long id,
            @Valid @RequestBody CarRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success("Car updated successfully", carService.updateCar(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCar(@PathVariable Long id) {
        carService.deleteCar(id);
        return ResponseEntity.ok(ApiResponse.success("Car deleted successfully", null));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> updateCarStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCarStatusRequest request) {

        carService.updateCarStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Car status updated", null));
    }

    @PostMapping("/{id}/images")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CarResponse>> uploadCarImages(
            @PathVariable Long id,
            @RequestParam("images") MultipartFile[] images) {

        CarResponse car = carService.uploadCarImages(id, images);
        return ResponseEntity.ok(ApiResponse.success("Car images uploaded successfully", car));
    }

    @DeleteMapping("/{id}/images/{imageId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCarImage(
            @PathVariable Long id,
            @PathVariable Long imageId) {

        carService.deleteCarImage(id, imageId);
        return ResponseEntity.ok(ApiResponse.success("Car image deleted successfully", null));
    }
}
