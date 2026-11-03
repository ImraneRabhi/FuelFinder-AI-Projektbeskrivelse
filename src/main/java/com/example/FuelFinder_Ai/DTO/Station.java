package com.example.FuelFinder_Ai.DTO;

import java.util.List;

public record Station(
        String company,
        String name,
        String address,
        double latitude,
        double longitude,
        String lastUpdate,
        List<FuelPrice> prices
) {}