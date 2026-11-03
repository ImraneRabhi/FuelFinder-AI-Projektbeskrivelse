package com.example.FuelFinder_Ai.DTO.fuelprices;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FpStationInfo(
        long id,
        String name,
        String address,
        double latitude,
        double longitude,
        @JsonProperty("last_update") String lastUpdate,
        @JsonProperty("station_category") String stationCategory
) {}