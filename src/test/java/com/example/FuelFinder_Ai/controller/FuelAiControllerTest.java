
package com.example.FuelFinder_Ai.controller;

import com.example.FuelFinder_Ai.DTO.AiRequest;
import com.example.FuelFinder_Ai.DTO.FuelRecommendation;
import com.example.FuelFinder_Ai.service.FuelAiService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FuelAiControllerTest {

    @Mock
    private FuelAiService fuelAiService;

    @InjectMocks
    private FuelAiController controller;

    @Test
    void shouldReturnAiRecommendation() {

        // Arrange
        String question = "Hvor skal jeg tanke?";
        String stationData = "Circle K: 13,50 kr./L, 3 km væk";

        AiRequest request = new AiRequest(question, stationData);

        FuelRecommendation expected = new FuelRecommendation(
                "Circle K",
                13.50,
                3.0,
                null,
                "Circle K ligger tæt på."
        );

        when(fuelAiService.askAi(question, stationData))
                .thenReturn(expected);

        // Act
        FuelRecommendation result = controller.askai(request);

        // Assert
        assertEquals(expected, result);

        verify(fuelAiService).askAi(question, stationData);
    }
}
