package com.example.FuelFinder_Ai.service;

import com.example.FuelFinder_Ai.DTO.FuelRecommendation;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FuelAiServiceTest {

    @Test
    void shouldReturnNullWhenStationDataIsMissing() {

        // Arrange
        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        ChatClient chatClient = mock(ChatClient.class);

        when(builder.build()).thenReturn(chatClient);

        FuelAiService service = new FuelAiService(builder);

        // Act
        FuelRecommendation result = service.askAi(
                "Hvor skal jeg tanke?",
                ""
        );

        // Assert
        assertNotNull(result);
        assertNull(result.recommendedStation());
        assertNull(result.price());
        assertNull(result.distance());
        assertNull(result.totalSavings());

        assertEquals(
                "Der mangler tankstationsdata for at kunne give en anbefaling.",
                result.reason()
        );

        verifyNoInteractions(chatClient);
    }
}
