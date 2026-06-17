package com.icet.carrental.service;

import com.icet.carrental.dto.response.CarResponse;
import com.icet.carrental.enums.CarStatus;

import java.util.List;

public interface FleetService {

    List<CarResponse> getAvailableCars();

    List<CarResponse> getCarsUnderMaintenance();

    CarResponse updateCarStatus(Long id, CarStatus status);
}
