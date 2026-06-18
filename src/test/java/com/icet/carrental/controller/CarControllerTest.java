package com.icet.carrental.controller;

import com.icet.carrental.dto.response.CarResponse;
import com.icet.carrental.enums.CarStatus;
import com.icet.carrental.enums.FuelType;
import com.icet.carrental.security.JwtUtil;
import com.icet.carrental.security.UserDetailsServiceImpl;
import com.icet.carrental.service.CarService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CarController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class CarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CarService carService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void uploadCarImages_returnsUpdatedCar() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "images", "photo.jpg", "image/jpeg", "image-data".getBytes());

        CarResponse carResponse = CarResponse.builder()
                .id(1L)
                .brand("Toyota")
                .model("Camry")
                .fuelType(FuelType.PETROL)
                .seatingCapacity(5)
                .dailyRate(BigDecimal.valueOf(50))
                .status(CarStatus.AVAILABLE)
                .description("Reliable sedan")
                .imageUrls(List.of("https://example.com/photo.jpg"))
                .build();

        when(carService.uploadCarImages(eq(1L), any())).thenReturn(carResponse);

        mockMvc.perform(multipart("/api/cars/1/images").file(image))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.imageUrls[0]").value("https://example.com/photo.jpg"));

        verify(carService).uploadCarImages(eq(1L), any());
    }
}
