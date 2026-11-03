package com.example.FuelFinder_Ai.client;

import com.example.FuelFinder_Ai.DTO.fuelprices.FpStationResponse;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class FuelPricesClient {
    private final RestClient client;

    public FuelPricesClient(RestClient.Builder builder,
                            @Value("${fuelprices.api-key}") String apiKey) {
        client = builder
                .baseUrl("https://fuelprices.dk/api")
                .defaultHeader("X-API-KEY", apiKey)
                .build();
    }

    public List<FpStationResponse> fetchAllStations() {
        FpStationResponse[] stations = client.get()
                .uri("/v1/stations/all?station_category={category}", "fuel")
                .retrieve()
                .body(FpStationResponse[].class);
        return stations == null ? List.of() : List.of(stations);
    }
}