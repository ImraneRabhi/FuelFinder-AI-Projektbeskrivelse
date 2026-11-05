
package com.example.FuelFinder_Ai.service;

import com.example.FuelFinder_Ai.DTO.Coordinates;
import com.example.FuelFinder_Ai.DTO.Station;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class StationDistanceService {

    public List<Station> findStationsWithinRadius(
            Map<Station, Double> drivingDistances,
            double radiusKm
    ) {
        return drivingDistances.entrySet().stream()
                .filter(entry ->
                        entry.getValue() <= radiusKm
                )
                .map(Map.Entry::getKey)
                .toList();
    }

    public List<Station> findNearestStations(
            List<Station> stations,
            Map<Station, Double> drivingDistances
    ) {
        return stations.stream()
                .sorted(Comparator.comparingDouble(
                        drivingDistances::get
                ))
                .limit(5)
                .toList();
    }

    public List<Station> findCheapestStations(
            List<Station> stations,
            Map<Station, Double> drivingDistances
    ) {
        return stations.stream()
                .filter(station ->
                        station.prices() != null
                                && !station.prices().isEmpty()
                )
                .sorted(
                        Comparator
                                .comparing(
                                        (Station station) ->
                                                station.prices()
                                                        .get(0)
                                                        .price()
                                )
                                .thenComparingDouble(
                                        drivingDistances::get
                                )
                )
                .limit(5)
                .toList();
    }

    // Bruges til hurtig geografisk filtrering
    public double calculateDistance(
            double lat1,
            double lon1,
            double lat2,
            double lon2
    ) {
        final double earthRadius = 6371.0;

        double latDifference = Math.toRadians(lat2 - lat1);
        double lonDifference = Math.toRadians(lon2 - lon1);

        double a = Math.pow(Math.sin(latDifference / 2), 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.pow(Math.sin(lonDifference / 2), 2);

        double c = 2 * Math.atan2(
                Math.sqrt(a),
                Math.sqrt(1 - a)
        );

        return earthRadius * c;
    }

    public List<Station> prefilterStations(
            Coordinates userLocation,
            List<Station> stations,
            double radiusKm
    ) {
        return stations.stream()
                .filter(station ->
                        calculateDistance(
                                userLocation.latitude(),
                                userLocation.longitude(),
                                station.latitude(),
                                station.longitude()
                        ) <= radiusKm
                )
                .toList();
    }
}
