package com.icet.carrental.dto.request;

import com.icet.carrental.enums.CarStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCarStatusRequest {

    @NotNull(message = "Status is required")
    private CarStatus status;
}
