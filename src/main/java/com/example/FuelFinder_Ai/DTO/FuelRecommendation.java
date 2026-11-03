
package com.example.FuelFinder_Ai.DTO;

public record FuelRecommendation(
        String recommendedStation,
        Double price,
        Double distance,
        Double totalSavings,
        String reason
) {
}
