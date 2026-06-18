package com.icet.carrental.dto.request;

import com.icet.carrental.enums.FuelType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CarRequest {

    @NotBlank(message = "Brand is required")
    @Size(max = 100)
    private String brand;

    @NotBlank(message = "Model is required")
    @Size(max = 100)
    private String model;

    @NotNull(message = "Fuel type is required")
    private FuelType fuelType;

    @Min(value = 1, message = "Seating capacity must be at least 1")
    @Max(value = 50)
    private int seatingCapacity;

    @NotNull(message = "Daily rate is required")
    @DecimalMin(value = "0.01", message = "Daily rate must be positive")
    private BigDecimal dailyRate;

    @Min(value = 1900)
    private Integer year;

    @Size(max = 20)
    private String licensePlate;

    @Size(max = 2000)
    private String description;

    @Size(max = 5, message = "A car can have at most 5 images")
    private List<@NotBlank @Size(max = 500) String> imageUrls;
}
