
package com.example.FuelFinder_Ai.controller;

import com.example.FuelFinder_Ai.DTO.*;
import com.example.FuelFinder_Ai.service.*;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class FuelAiController {

    private final FuelAiService fuelAiService;
    private final FuelPriceService fuelPriceService;
    private final GeocodingService geocodingService;
    private final StationDistanceService distanceService;

    public FuelAiController(
            FuelAiService fuelAiService,
            FuelPriceService fuelPriceService,
            GeocodingService geocodingService,
            StationDistanceService distanceService
    ) {
        this.fuelAiService = fuelAiService;
        this.fuelPriceService = fuelPriceService;
        this.geocodingService = geocodingService;
        this.distanceService = distanceService;
    }

    @PostMapping("/api/fuelfinder/askai")
    public FuelRecommendation askai(@RequestBody AiRequest request) {

        // 1. Find koordinater ud fra brugerens adresse
        Coordinates userLocation =
                geocodingService.findCoordinates(request.location());

        // 2. Find brændstoftypen
        FuelType fuelType = FuelType.from(request.fuelType());

        // 3. Hent tankstationer og priser
        List<Station> allStations =
                fuelPriceService.getStations(fuelType);

        // 4. Find de 5 nærmeste tankstationer
        List<Station> nearestStations =
                distanceService.findNearestStations(
                        userLocation,
                        allStations
                );

        if (nearestStations.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Ingen tankstationer med priser blev fundet."
            );
        }

        // 5. Opret resultat med afstand og Google Maps-link
        List<StationResult> stationResults =
                nearestStations.stream()
                        .map(station -> {

                            double distance =
                                    distanceService.calculateDistance(
                                            userLocation.latitude(),
                                            userLocation.longitude(),
                                            station.latitude(),
                                            station.longitude()
                                    );

                            double roundedDistance =
                                    Math.round(distance * 1000.0) / 1000.0;

                            double price =
                                    station.prices().get(0)
                                            .price().doubleValue();

                            String directionsUrl =
                                    createDirectionsUrl(station);

                            return new StationResult(
                                    station.company() + " - " + station.name(),
                                    station.address(),
                                    price,
                                    roundedDistance,
                                    directionsUrl
                            );
                        })
                        .toList();

        // 6. Oplysninger til Mistral AI
        String stationData =
                "Brugerens område: " + request.location()
                        + "\nBrændstoftype: " + fuelType
                        + "\nBilens forbrug (L/100 km): "
                        + request.consumptionPer100Km()
                        + "\nBrændstof tilbage (liter): "
                        + request.remainingFuelLiters()
                        + "\nTankkapacitet (liter): "
                        + request.tankCapacityLiters()
                        + "\nAfstande er i fugleflugtslinje, ikke køreafstand."
                        + "\nTankstationer:\n"
                        + stationResults;

        // 7. Hent anbefaling fra Mistral
        FuelRecommendation aiResult =
                fuelAiService.askAi(
                        request.question(),
                        stationData
                );

        // 8. Returner AI-anbefaling og alle 5 stationer
        return new FuelRecommendation(
                aiResult.recommendedStation(),
                aiResult.price(),
                aiResult.distance(),
                aiResult.totalSavings(),
                aiResult.reason(),
                stationResults
        );
    }

    // Opret Google Maps-link til tankstationen
    private String createDirectionsUrl(Station station) {

        return "https://www.google.com/maps/dir/?api=1"
                + "&destination="
                + station.latitude()
                + "%2C"
                + station.longitude()
                + "&travelmode=driving";
    }
}
