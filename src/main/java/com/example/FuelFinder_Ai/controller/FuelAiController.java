
package com.example.FuelFinder_Ai.controller;

import com.example.FuelFinder_Ai.DTO.*;
import com.example.FuelFinder_Ai.service.*;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@RestController
public class FuelAiController {

    private final FuelAiService fuelAiService;
    private final FuelPriceService fuelPriceService;
    private final GeocodingService geocodingService;
    private final StationDistanceService distanceService;
    private final RoutingService routingService;
    private final FuelRecommendationService recommendationService;

    public FuelAiController(
            FuelAiService fuelAiService,
            FuelPriceService fuelPriceService,
            GeocodingService geocodingService,
            StationDistanceService distanceService,
            RoutingService routingService,
            FuelRecommendationService recommendationService
    ) {
        this.fuelAiService = fuelAiService;
        this.fuelPriceService = fuelPriceService;
        this.geocodingService = geocodingService;
        this.distanceService = distanceService;
        this.routingService = routingService;
        this.recommendationService = recommendationService;
    }

    @PostMapping("/api/fuelfinder/askai")
    public FuelAiResponse askai(@RequestBody AiRequest request) {

        // 1. Validér input
        if (request.radiusKm() == null
                || !Double.isFinite(request.radiusKm())
                || request.radiusKm() <= 0
                || request.consumptionPer100Km() == null
                || request.remainingFuelLiters() == null
                || request.tankCapacityLiters() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Radius og biloplysninger skal udfyldes korrekt."
            );
        }

        // 2. Find brugerens koordinater
        Coordinates userLocation =
                geocodingService.findCoordinates(request.location());

        FuelType fuelType =
                FuelType.from(request.fuelType());

        List<Station> allStations =
                fuelPriceService.getStations(fuelType);

        // 3. Indledende geografisk filtrering
        List<Station> candidates =
                distanceService.prefilterStations(
                        userLocation,
                        allStations,
                        request.radiusKm()
                );

        if (candidates.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Ingen tankstationer fundet i området."
            );
        }

        // 4. Hent faktiske køreafstande
        Map<Station, Double> drivingDistances =
                routingService.getDrivingDistances(
                        userLocation,
                        candidates
                );

        List<Station> withinRadius =
                distanceService.findStationsWithinRadius(
                        drivingDistances,
                        request.radiusKm()
                );

        if (withinRadius.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Ingen tankstationer inden for køreradius."
            );
        }

        // 5. Find de fem nærmeste
        List<Station> nearest =
                distanceService.findNearestStations(
                        withinRadius,
                        drivingDistances
                );

        // 6. Find de fem billigste
        List<Station> cheapest =
                distanceService.findCheapestStations(
                        withinRadius,
                        drivingDistances
                );

        List<StationResult> nearestResults =
                nearest.stream()
                        .map(station -> createStationResult(
                                userLocation,
                                station,
                                drivingDistances.get(station)
                        ))
                        .toList();

        List<StationResult> cheapestResults =
                cheapest.stream()
                        .map(station -> createStationResult(
                                userLocation,
                                station,
                                drivingDistances.get(station)
                        ))
                        .toList();

        // 7. Beregn anbefalingen ud fra ALLE stationer
        // inden for radius, ikke kun top 5.
        List<StationResult> allResults =
                withinRadius.stream()
                        .map(station -> createStationResult(
                                userLocation,
                                station,
                                drivingDistances.get(station)
                        ))
                        .toList();

        FuelRecommendationService.CalculationResult calculation;

        try {
            calculation = recommendationService.calculate(
                    allResults,
                    request.consumptionPer100Km(),
                    request.remainingFuelLiters(),
                    request.tankCapacityLiters()
            );
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage()
            );
        }

        // 8. Hvis ingen station kan nås
        if (calculation.recommendedStation() == null) {
            return new FuelAiResponse(
                    null,
                    null,
                    null,
                    null,
                    calculation.explanation(),
                    nearestResults,
                    cheapestResults
            );
        }

        // 9. Mistral forklarer Java-beregningen
        String calculationData =
                "Anbefalet station: "
                        + calculation.recommendedStation()
                        + "\nPris pr. liter: "
                        + calculation.price()
                        + "\nKøreafstand i km: "
                        + calculation.distance()
                        + "\nTeoretisk rækkevidde i km: "
                        + calculation.rangeKm()
                        + "\nEstimeret besparelse i kr.: "
                        + calculation.totalSavings()
                        + "\nBeregningsmetode: "
                        + calculation.explanation();

        String reason = fuelAiService.explainRecommendation(
                request.question(),
                calculationData
        );

        // 10. Returnér Java-beregningen og AI-forklaringen
        return new FuelAiResponse(
                calculation.recommendedStation(),
                calculation.price(),
                calculation.distance(),
                calculation.totalSavings(),
                reason,
                nearestResults,
                cheapestResults
        );
    }

    private StationResult createStationResult(
            Coordinates userLocation,
            Station station,
            double drivingDistanceKm
    ) {
        return new StationResult(
                station.company() + " - " + station.name(),
                station.address(),
                station.prices().get(0).price().doubleValue(),
                Math.round(drivingDistanceKm * 1000.0) / 1000.0,
                createDirectionsUrl(userLocation, station)
        );
    }

    private String createDirectionsUrl(
            Coordinates userLocation,
            Station station
    ) {
        return "https://www.google.com/maps/dir/?api=1"
                + "&origin="
                + userLocation.latitude()
                + "%2C"
                + userLocation.longitude()
                + "&destination="
                + station.latitude()
                + "%2C"
                + station.longitude()
                + "&travelmode=driving";
    }
}
