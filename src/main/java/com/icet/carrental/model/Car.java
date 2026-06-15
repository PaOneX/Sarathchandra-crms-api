package com.icet.carrental.model;

import com.icet.carrental.enums.CarStatus;
import com.icet.carrental.enums.FuelType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Car {
    private Long          id;
    private String        brand;
    private String        model;
    private FuelType      fuelType;
    private int           seatingCapacity;
    private BigDecimal    dailyRate;
    private CarStatus     status;
    private Integer       year;
    private String        licensePlate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
