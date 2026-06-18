package com.icet.carrental.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarImage {
    private Long          id;
    private Long          carId;
    private String        storagePath;
    private String        url;
    private int           sortOrder;
    private LocalDateTime createdAt;
}
