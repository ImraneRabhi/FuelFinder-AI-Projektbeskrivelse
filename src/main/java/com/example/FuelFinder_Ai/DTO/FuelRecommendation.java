
package com.example.FuelFinder_Ai.DTO;

import java.util.List;

public record FuelRecommendation(
        String recommendedStation,
        Double price,
        Double distance,
        Double totalSavings,
        String reason,
        List<StationResult> stations
) {
    // Bevarer kompatibilitet med den eksisterende AI-service
    public FuelRecommendation(
            String recommendedStation,
            Double price,
            Double distance,
            Double totalSavings,
            String reason
    ) {
        this(
                recommendedStation,
                price,
                distance,
                totalSavings,
                reason,
                List.of()
        );
    }
}
