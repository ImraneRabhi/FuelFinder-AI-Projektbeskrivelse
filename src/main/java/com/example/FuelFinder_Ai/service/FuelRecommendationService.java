
package com.example.FuelFinder_Ai.service;

import com.example.FuelFinder_Ai.DTO.StationResult;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class FuelRecommendationService {

    private static final double RESERVE_KM = 10.0;

    public CalculationResult calculate(
            List<StationResult> stations,
            double consumptionPer100Km,
            double remainingFuelLiters,
            double tankCapacityLiters
    ) {

        // 1. Valider input
        if (stations == null || stations.isEmpty()) {
            throw new IllegalArgumentException(
                    "Ingen tankstationer at sammenligne."
            );
        }

        if (!Double.isFinite(consumptionPer100Km)
                || consumptionPer100Km <= 0
                || !Double.isFinite(remainingFuelLiters)
                || remainingFuelLiters < 0
                || !Double.isFinite(tankCapacityLiters)
                || tankCapacityLiters <= 0
                || remainingFuelLiters > tankCapacityLiters) {

            throw new IllegalArgumentException(
                    "Ugyldige oplysninger om bilens brændstof."
            );
        }

        // 2. Teoretisk rækkevidde
        double rangeKm =
                remainingFuelLiters
                        / consumptionPer100Km * 100.0;

        // Brændstofreserve svarende til 10 km
        double reserveLiters =
                fuelUsed(RESERVE_KM, consumptionPer100Km);

        // 3. Find stationer, der kan nås
        List<StationResult> reachable =
                stations.stream()
                        .filter(station ->
                                station.distanceKm() != null
                                        && station.price() != null
                                        && Double.isFinite(station.distanceKm())
                                        && station.distanceKm() >= 0
                                        && Double.isFinite(station.price())
                                        && station.price() > 0
                        )
                        .filter(station ->
                                remainingFuelLiters
                                        - fuelUsed(
                                        station.distanceKm(),
                                        consumptionPer100Km
                                ) >= reserveLiters
                        )
                        .toList();

        if (reachable.isEmpty()) {
            return new CalculationResult(
                    null,
                    null,
                    null,
                    null,
                    round(rangeKm),
                    "Ingen tankstation kan nås med "
                            + "den valgte sikkerhedsmargin på 10 km."
            );
        }

        // 4. Find nærmeste tilgængelige station
        StationResult nearest =
                reachable.stream()
                        .min(Comparator.comparingDouble(
                                StationResult::distanceKm
                        ))
                        .orElseThrow();

        // 5. Find den økonomisk bedste station
        StationResult best =
                reachable.stream()
                        .min(
                                Comparator
                                        .comparingDouble(
                                                (StationResult station) ->
                                                        totalCost(
                                                                station,
                                                                consumptionPer100Km,
                                                                remainingFuelLiters,
                                                                tankCapacityLiters
                                                        )
                                        )
                                        .thenComparingDouble(
                                                StationResult::distanceKm
                                        )
                        )
                        .orElseThrow();

        // 6. Beregn økonomien for begge stationer
        double nearestCost =
                totalCost(
                        nearest,
                        consumptionPer100Km,
                        remainingFuelLiters,
                        tankCapacityLiters
                );

        double bestCost =
                totalCost(
                        best,
                        consumptionPer100Km,
                        remainingFuelLiters,
                        tankCapacityLiters
                );

        double savings =
                Math.max(0.0, nearestCost - bestCost);

        return new CalculationResult(
                best.name(),
                best.price(),
                best.distanceKm(),
                round(savings),
                round(rangeKm),
                "Estimeret besparelse sammenlignet med "
                        + "den nærmeste tilgængelige tankstation. "
                        + "Beregningen omfatter literprisen og "
                        + "brændstofforbruget på køreturen til stationen. "
                        + "Hjemturen er ikke medregnet."
        );
    }

    // Brændstofforbrug på turen
    private double fuelUsed(
            double distanceKm,
            double consumptionPer100Km
    ) {
        return distanceKm * consumptionPer100Km / 100.0;
    }

    // Sammenlign samme brændstofmængde
    // og medregn forbruget på turen
    private double totalCost(
            StationResult station,
            double consumptionPer100Km,
            double remainingFuelLiters,
            double tankCapacityLiters
    ) {

        double litersToFill =
                tankCapacityLiters - remainingFuelLiters;

        double drivingFuel =
                fuelUsed(
                        station.distanceKm(),
                        consumptionPer100Km
                );

        return (litersToFill + drivingFuel)
                * station.price();
    }

    private double round(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }

    public record CalculationResult(
            String recommendedStation,
            Double price,
            Double distance,
            Double totalSavings,
            Double rangeKm,
            String explanation
    ) {
    }
}
