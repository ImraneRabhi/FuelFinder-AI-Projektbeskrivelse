
package com.example.FuelFinder_Ai.controller;

import com.example.FuelFinder_Ai.DTO.AiRequest;
import com.example.FuelFinder_Ai.DTO.FuelRecommendation;
import com.example.FuelFinder_Ai.DTO.FuelType;
import com.example.FuelFinder_Ai.DTO.Station;
import com.example.FuelFinder_Ai.service.FuelAiService;
import com.example.FuelFinder_Ai.service.FuelPriceService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FuelAiControllerTest {

    @Mock
    private FuelAiService fuelAiService;

    @Mock
    private FuelPriceService fuelPriceService;

    @InjectMocks
    private FuelAiController controller;

    @Test
    void shouldReturnAiRecommendation() {

        // Arrange
        AiRequest request = new AiRequest(
                "Hvor skal jeg tanke?",
                "diesel",
                6.5,
                10.0,
                50.0
        );

        List<Station> stations = List.of(
                new Station(
                        "Circle K",
                        "Circle K Teststation",
                        "Testvej 1",
                        55.67,
                        12.56,
                        "2026-11-04",
                        List.of()
                )
        );

        FuelRecommendation expected = new FuelRecommendation(
                "Circle K Teststation",
                null,
                null,
                null,
                "Anbefaling fra AI"
        );

        when(fuelPriceService.getStations(FuelType.DIESEL))
                .thenReturn(stations);

        when(fuelAiService.askAi(
                request.question(),
                stations.toString()
        )).thenReturn(expected);

        // Act
        FuelRecommendation result = controller.askai(request);

        // Assert
        assertEquals(expected, result);

        verify(fuelPriceService).getStations(FuelType.DIESEL);

        verify(fuelAiService).askAi(
                request.question(),
                stations.toString()
        );
    }
}
