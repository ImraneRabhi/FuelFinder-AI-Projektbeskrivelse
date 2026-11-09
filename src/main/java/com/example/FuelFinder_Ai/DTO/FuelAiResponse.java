
package com.example.FuelFinder_Ai.DTO;

import java.util.List;

public record FuelAiResponse(
        String recommendedStation,
        Double price,
        Double distance,
        Double totalSavings,
        String reason,
        List<StationResult> nearestStations,
        List<StationResult> cheapestStations
) {
}
