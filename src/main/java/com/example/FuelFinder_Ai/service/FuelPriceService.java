package com.example.FuelFinder_Ai.service;

import com.example.FuelFinder_Ai.DTO.FuelPrice;
import com.example.FuelFinder_Ai.DTO.FuelType;
import com.example.FuelFinder_Ai.DTO.Station;
import com.example.FuelFinder_Ai.DTO.fuelprices.FpStationResponse;
import com.example.FuelFinder_Ai.client.FuelPricesClient;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FuelPriceService {
    private final FuelPricesClient client;

    public FuelPriceService(FuelPricesClient client) {
        this.client = client;
    }

    public List<Station> getStations(FuelType fuelType) {
        List<FpStationResponse> responses;
        try {
            responses = client.fetchAllStations();
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Brændstofpriserne kan ikke hentes lige nu. Prøv igen senere.", e);
        }

        return responses.stream()
                .filter(s -> s.station() != null && s.prices() != null)
                .map(s -> toStation(s, fuelType))
                .filter(s -> !s.prices().isEmpty())
                .sorted(Comparator.comparing((Station s) -> s.prices().getFirst().price()))
                .toList();
    }

    private Station toStation(FpStationResponse response, FuelType fuelType) {
        String companyName = response.company() == null ? null : response.company().company();

        return new Station(
                companyName,
                response.station().name(),
                response.station().address(),
                response.station().latitude(),
                response.station().longitude(),
                response.station().lastUpdate(),
                toPrices(response.prices(), fuelType)
        );
    }


    private List<FuelPrice> toPrices(Map<String, String> prices, FuelType fuelType) {
        return prices.entrySet().stream()
                .filter(e -> fuelType.matches(e.getKey()))
                .map(e -> toFuelPrice(e.getKey(), e.getValue()))
                .filter(Objects::nonNull)
                .min(Comparator.comparing(FuelPrice::price))
                .map(List::of)
                .orElse(List.of());
    }

    private FuelPrice toFuelPrice(String product, String price) {
        try {
            return new FuelPrice(product, new BigDecimal(price));
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }
}