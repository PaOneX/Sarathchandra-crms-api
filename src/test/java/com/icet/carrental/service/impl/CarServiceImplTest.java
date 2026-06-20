package com.icet.carrental.service.impl;

import com.icet.carrental.config.SupabaseProperties;
import com.icet.carrental.dto.response.CarResponse;
import com.icet.carrental.enums.CarStatus;
import com.icet.carrental.enums.FuelType;
import com.icet.carrental.exception.InvalidFileException;
import com.icet.carrental.model.Car;
import com.icet.carrental.model.CarImage;
import com.icet.carrental.repository.CarImageRepository;
import com.icet.carrental.repository.CarRepository;
import com.icet.carrental.service.storage.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CarServiceImplTest {

    @Mock private CarRepository      carRepository;
    @Mock private CarImageRepository carImageRepository;
    @Mock private StorageService     storageService;
    @Mock private SupabaseProperties supabaseProperties;

    @InjectMocks
    private CarServiceImpl carService;

    @BeforeEach
    void setUp() {
        SupabaseProperties.StorageBuckets buckets = new SupabaseProperties.StorageBuckets();
        buckets.setCars("car-images");
        when(supabaseProperties.getStorage()).thenReturn(buckets);
    }

    @Test
    void uploadCarImages_uploadsAndPersistsImages() {
        Car car = sampleCar(1L);
        MockMultipartFile file = new MockMultipartFile(
                "images", "photo.jpg", "image/jpeg", "image-data".getBytes());

        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        when(carImageRepository.countByCarId(1L)).thenReturn(0);
        when(storageService.upload(eq("car-images"), any(), any(), eq("image/jpeg")))
                .thenReturn("https://test.supabase.co/storage/v1/object/public/car-images/cars/1/uuid.jpg");
        when(carImageRepository.findByCarId(1L)).thenReturn(List.of(
                CarImage.builder()
                        .id(10L)
                        .carId(1L)
                        .storagePath("cars/1/uuid.jpg")
                        .url("https://test.supabase.co/storage/v1/object/public/car-images/cars/1/uuid.jpg")
                        .sortOrder(0)
                        .createdAt(LocalDateTime.now())
                        .build()
        ));

        CarResponse response = carService.uploadCarImages(1L, new MockMultipartFile[]{file});

        assertThat(response.getImageUrls()).hasSize(1);
        assertThat(response.getImageUrls().get(0)).contains("cars/1");

        ArgumentCaptor<CarImage> captor = ArgumentCaptor.forClass(CarImage.class);
        verify(carImageRepository).save(captor.capture());
        assertThat(captor.getValue().getCarId()).isEqualTo(1L);
        assertThat(captor.getValue().getSortOrder()).isZero();
    }

    @Test
    void uploadCarImages_rejectsWhenMaxImagesExceeded() {
        Car car = sampleCar(1L);
        MockMultipartFile file = new MockMultipartFile(
                "images", "photo.jpg", "image/jpeg", "image-data".getBytes());

        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        when(carImageRepository.countByCarId(1L)).thenReturn(5);

        assertThatThrownBy(() -> carService.uploadCarImages(1L, new MockMultipartFile[]{file}))
                .isInstanceOf(InvalidFileException.class)
                .hasMessageContaining("at most 5 images");
    }

    @Test
    void deleteCarImage_deletesFromStorageAndDatabase() {
        Car car = sampleCar(1L);
        CarImage image = CarImage.builder()
                .id(10L)
                .carId(1L)
                .storagePath("cars/1/uuid.jpg")
                .url("https://example.com/image.jpg")
                .sortOrder(0)
                .build();

        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        when(carImageRepository.findById(10L)).thenReturn(Optional.of(image));

        carService.deleteCarImage(1L, 10L);

        verify(storageService).delete("car-images", "cars/1/uuid.jpg");
        verify(carImageRepository).deleteById(10L);
    }

    @Test
    void toCarResponse_includesDescriptionAndImageUrls() {
        Car car = sampleCar(2L);
        car.setDescription("Comfortable sedan");

        when(carImageRepository.findByCarId(2L)).thenReturn(List.of(
                CarImage.builder().id(1L).carId(2L).url("https://example.com/a.jpg").build(),
                CarImage.builder().id(2L).carId(2L).url("https://example.com/b.jpg").build()
        ));

        CarResponse response = carService.toCarResponse(car);

        assertThat(response.getDescription()).isEqualTo("Comfortable sedan");
        assertThat(response.getImageUrls()).containsExactly(
                "https://example.com/a.jpg",
                "https://example.com/b.jpg"
        );
    }

    private Car sampleCar(Long id) {
        return Car.builder()
                .id(id)
                .brand("Toyota")
                .model("Camry")
                .fuelType(FuelType.PETROL)
                .seatingCapacity(5)
                .dailyRate(BigDecimal.valueOf(50))
                .status(CarStatus.AVAILABLE)
                .year(2024)
                .licensePlate("ABC-123")
                .createdAt(LocalDateTime.now())
                .build();
    }
}
