
package com.example.FuelFinder_Ai.service;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FuelAiServiceTest {

    @Test
    void shouldCreateFuelAiService() {

        // Arrange
        ChatClient.Builder builder =
                mock(ChatClient.Builder.class);

        ChatClient chatClient =
                mock(ChatClient.class);

        when(builder.build()).thenReturn(chatClient);

        // Act
        FuelAiService service = new FuelAiService(builder);

        // Assert
        assertNotNull(service);

        verify(builder).build();
    }
}
