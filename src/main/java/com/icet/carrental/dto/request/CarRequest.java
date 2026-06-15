package com.icet.carrental.dto.request;

import com.icet.carrental.enums.FuelType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

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
}
