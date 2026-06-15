package com.icet.carrental.controller;

import com.icet.carrental.dto.request.UpdateCarStatusRequest;
import com.icet.carrental.dto.response.ApiResponse;
import com.icet.carrental.dto.response.CarResponse;
import com.icet.carrental.service.FleetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fleet")
@RequiredArgsConstructor
public class FleetController {

    private final FleetService fleetService;

    @GetMapping("/availability")
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<List<CarResponse>>> getAvailableCars() {
        return ResponseEntity.ok(ApiResponse.success(fleetService.getAvailableCars()));
    }

    @GetMapping("/maintenance")
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<List<CarResponse>>> getCarsUnderMaintenance() {
        return ResponseEntity.ok(ApiResponse.success(fleetService.getCarsUnderMaintenance()));
    }

    @PatchMapping("/cars/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<CarResponse>> updateCarStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCarStatusRequest request) {

        return ResponseEntity.ok(ApiResponse.success("Status updated",
                fleetService.updateCarStatus(id, request.getStatus())));
    }
}
