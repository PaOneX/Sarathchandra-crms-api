package com.icet.carrental.dto.response;

import com.icet.carrental.enums.CarStatus;
import com.icet.carrental.enums.FuelType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CarResponse {
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
}
