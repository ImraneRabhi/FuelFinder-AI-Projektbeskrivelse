
package com.example.FuelFinder_Ai.DTO;

public record StationResult(
        String name,
        String address,
        Double price,
        Double distanceKm,
        String directionsUrl
) {
}
