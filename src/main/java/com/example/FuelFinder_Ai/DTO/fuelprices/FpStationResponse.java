package com.example.FuelFinder_Ai.DTO.fuelprices;

import java.util.Map;

public record FpStationResponse(
        FpCompany company,
        FpStationInfo station,
        Map<String, String> prices
) {}