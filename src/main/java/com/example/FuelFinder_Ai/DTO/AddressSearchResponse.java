package com.example.FuelFinder_Ai.DTO;
import java.util.List;

public record AddressSearchResponse(
        String status,
        String description,
        List<AddressHit> fund
) {

    public record AddressHit(
            String id,
            String type,
            String titel
    ) {
    }
}