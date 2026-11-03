package com.example.FuelFinder_Ai;

import com.example.FuelFinder_Ai.DTO.AddressResult;
import com.example.FuelFinder_Ai.service.CoordinateService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
class CoordinateServiceTest {

    private final CoordinateService coordinateService =
            new CoordinateService();

    @Test
    void shouldConvertEpsg25832ToLatitudeAndLongitude() {

        AddressResult result = coordinateService.convert(
                "Testadresse",
                500000,
                0
        );

        assertEquals("Testadresse", result.address());
        assertEquals(0.0, result.latitude(), 0.0001);
        assertEquals(9.0, result.longitude(), 0.0001);
    }
}