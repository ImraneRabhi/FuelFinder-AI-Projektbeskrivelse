
package com.example.FuelFinder_Ai.service;

import com.example.FuelFinder_Ai.DTO.Coordinates;
import com.example.FuelFinder_Ai.DTO.Station;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class StationDistanceService {

    public List<Station> findNearestStations(
            Coordinates userLocation,
            List<Station> stations
    ) {
        return stations.stream()
                .sorted(Comparator.comparingDouble(station ->
                        calculateDistance(
                                userLocation.latitude(),
                                userLocation.longitude(),
                                station.latitude(),
                                station.longitude()
                        )
                ))
                .limit(5)
                .toList();
    }

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
}
