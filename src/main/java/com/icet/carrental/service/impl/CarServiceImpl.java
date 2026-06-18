package com.icet.carrental.service.impl;

import com.icet.carrental.config.SupabaseProperties;
import com.icet.carrental.dto.request.CarRequest;
import com.icet.carrental.dto.response.CarResponse;
import com.icet.carrental.enums.CarStatus;
import com.icet.carrental.exception.InvalidFileException;
import com.icet.carrental.exception.ResourceNotFoundException;
import com.icet.carrental.model.Car;
import com.icet.carrental.model.CarImage;
import com.icet.carrental.repository.CarImageRepository;
import com.icet.carrental.repository.CarRepository;
import com.icet.carrental.service.CarService;
import com.icet.carrental.service.storage.StorageService;
import com.icet.carrental.util.ImageFileValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {

    private static final int MAX_IMAGES_PER_CAR = 5;

    private final CarRepository      carRepository;
    private final CarImageRepository carImageRepository;
    private final StorageService     storageService;
    private final SupabaseProperties supabaseProperties;

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
                .description(request.getDescription())
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
        car.setDescription(request.getDescription());

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

    @Override
    @Transactional
    public CarResponse uploadCarImages(Long carId, MultipartFile[] files) {
        Car car = findCarOrThrow(carId);

        if (files == null || files.length == 0) {
            throw new InvalidFileException("At least one image file is required");
        }

        int existingCount = carImageRepository.countByCarId(carId);
        if (existingCount + files.length > MAX_IMAGES_PER_CAR) {
            throw new InvalidFileException(
                    "A car can have at most " + MAX_IMAGES_PER_CAR + " images");
        }

        String bucket = supabaseProperties.getStorage().getCars();
        int    order  = existingCount;

        for (MultipartFile file : files) {
            ImageFileValidator.validate(file);

            String extension = ImageFileValidator.resolveExtension(file);
            String path      = ImageFileValidator.generateObjectName("cars/" + carId, extension);
            String contentType = ImageFileValidator.normalizeContentType(file.getContentType());

            String publicUrl = storageService.upload(
                    bucket, path, ImageFileValidator.readBytes(file), contentType);

            carImageRepository.save(CarImage.builder()
                    .carId(carId)
                    .storagePath(path)
                    .url(publicUrl)
                    .sortOrder(order++)
                    .build());
        }

        return toCarResponse(car);
    }

    @Override
    @Transactional
    public void deleteCarImage(Long carId, Long imageId) {
        findCarOrThrow(carId);

        CarImage image = carImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Car image", imageId));

        if (!image.getCarId().equals(carId)) {
            throw new ResourceNotFoundException("Car image", imageId);
        }

        storageService.delete(supabaseProperties.getStorage().getCars(), image.getStoragePath());
        carImageRepository.deleteById(imageId);
    }

    private Car findCarOrThrow(Long id) {
        return carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Car", id));
    }

    @Override
    public CarResponse toCarResponse(Car car) {
        List<String> imageUrls = carImageRepository.findByCarId(car.getId()).stream()
                .map(CarImage::getUrl)
                .toList();

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
                .description(car.getDescription())
                .imageUrls(imageUrls.isEmpty() ? List.of() : new ArrayList<>(imageUrls))
                .createdAt(car.getCreatedAt())
                .build();
    }
}
