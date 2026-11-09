
package com.example.FuelFinder_Ai.controller;

import com.example.FuelFinder_Ai.DTO.*;
import com.example.FuelFinder_Ai.service.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FuelAiControllerTest {

    @Mock
    private FuelAiService fuelAiService;

    @Mock
    private FuelPriceService fuelPriceService;

    @Mock
    private GeocodingService geocodingService;

    @Mock
    private StationDistanceService distanceService;

    @Mock
    private RoutingService routingService;

    @Mock
    private FuelRecommendationService recommendationService;

    @InjectMocks
    private FuelAiController controller;

    @Test
    void shouldReturnAiRecommendation() {

        // Arrange
        AiRequest request = new AiRequest(
                "Hvor skal jeg tanke?",
                "Brønshøj Torv",
                "diesel",
                6.0,
                1.0,
                50.0,
                5.0
        );

        Coordinates coordinates =
                new Coordinates(55.70, 12.50);

        // Tankstation med dieselpris
        Station station = new Station(
                "Circle K",
                "Teststation",
                "Testvej 1",
                55.67,
                12.56,
                "2026-11-04",
                List.of(
                        new FuelPrice(
                                "diesel",
                                new BigDecimal("18.39")
                        )
                )
        );

        List<Station> stations = List.of(station);

        Map<Station, Double> distances =
                Map.of(station, 1.59);

        FuelRecommendationService.CalculationResult calculation =
                new FuelRecommendationService.CalculationResult(
                        "Circle K - Teststation",
                        18.39,
                        1.59,
                        3.74,
                        16.667,
                        "Estimeret besparelse"
                );

        when(geocodingService.findCoordinates("Brønshøj Torv"))
                .thenReturn(coordinates);

        when(fuelPriceService.getStations(FuelType.DIESEL))
                .thenReturn(stations);

        when(distanceService.prefilterStations(
                eq(coordinates),
                eq(stations),
                eq(5.0)
        )).thenReturn(stations);

        when(routingService.getDrivingDistances(
                eq(coordinates),
                eq(stations)
        )).thenReturn(distances);

        when(distanceService.findStationsWithinRadius(
                eq(distances),
                eq(5.0)
        )).thenReturn(stations);

        when(distanceService.findNearestStations(
                eq(stations),
                eq(distances)
        )).thenReturn(stations);

        when(distanceService.findCheapestStations(
                eq(stations),
                eq(distances)
        )).thenReturn(stations);

        when(recommendationService.calculate(
                anyList(),
                eq(6.0),
                eq(1.0),
                eq(50.0)
        )).thenReturn(calculation);

        when(fuelAiService.explainRecommendation(
                eq(request.question()),
                anyString()
        )).thenReturn(
                "Circle K er den mest økonomiske station."
        );

        // Act
        FuelAiResponse result = controller.askai(request);

        // Assert
        assertNotNull(result);

        assertEquals(
                "Circle K - Teststation",
                result.recommendedStation()
        );

        assertEquals(18.39, result.price());
        assertEquals(1.59, result.distance());
        assertEquals(3.74, result.totalSavings());

        assertEquals(
                "Circle K er den mest økonomiske station.",
                result.reason()
        );

        // Kontroller at Java beregner anbefalingen
        verify(recommendationService).calculate(
                anyList(),
                eq(6.0),
                eq(1.0),
                eq(50.0)
        );

        // Kontroller at Mistral får korrekt rækkevidde
        verify(fuelAiService).explainRecommendation(
                eq(request.question()),
                argThat(data -> data.contains("16,67"))
        );
    }
}
