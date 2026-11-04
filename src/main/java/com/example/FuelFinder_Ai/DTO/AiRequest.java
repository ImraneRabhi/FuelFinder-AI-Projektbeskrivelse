
package com.example.FuelFinder_Ai.DTO;

public record AiRequest(
        String question,
        String location,
        String fuelType,
        Double consumptionPer100Km,
        Double remainingFuelLiters,
        Double tankCapacityLiters
) {
}
