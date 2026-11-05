
package com.example.FuelFinder_Ai.service;

import com.example.FuelFinder_Ai.DTO.Coordinates;
import com.example.FuelFinder_Ai.DTO.Station;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RoutingService {

    private static final int BATCH_SIZE = 40;

    private final RestClient client;

    public RoutingService(RestClient.Builder builder) {
        this.client = builder.build();
    }

    public Map<Station, Double> getDrivingDistances(
            Coordinates userLocation,
            List<Station> stations
    ) {
        Map<Station, Double> distances = new HashMap<>();

        for (int start = 0; start < stations.size();
             start += BATCH_SIZE) {

            List<Station> batch = stations.subList(
                    start,
                    Math.min(start + BATCH_SIZE, stations.size())
            );

            fetchBatch(userLocation, batch, distances);
        }

        return distances;
    }

    private void fetchBatch(
            Coordinates userLocation,
            List<Station> stations,
            Map<Station, Double> distances
    ) {
        if (stations.isEmpty()) {
            return;
        }

        StringBuilder coordinates = new StringBuilder();

        // OSRM bruger longitude,latitude
        coordinates.append(userLocation.longitude())
                .append(",")
                .append(userLocation.latitude());

        StringBuilder destinations = new StringBuilder();

        for (int i = 0; i < stations.size(); i++) {
            Station station = stations.get(i);

            coordinates.append(";")
                    .append(station.longitude())
                    .append(",")
                    .append(station.latitude());

            if (i > 0) {
                destinations.append(";");
            }

            destinations.append(i + 1);
        }

        String url =
                "https://router.project-osrm.org/table/v1/driving/"
                        + coordinates
                        + "?sources=0"
                        + "&destinations=" + destinations
                        + "&annotations=distance";

        try {
            RouteTableResponse response = client.get()
                    .uri(URI.create(url))
                    .retrieve()
                    .body(RouteTableResponse.class);

            if (response == null
                    || !"Ok".equals(response.code())
                    || response.distances() == null
                    || response.distances().length == 0) {

                throw new IllegalStateException(
                        "Ugyldigt svar fra OSRM"
                );
            }

            Double[] row = response.distances()[0];

            if (row == null || row.length != stations.size()) {
                throw new IllegalStateException(
                        "OSRM returnerede et uventet antal afstande"
                );
            }

            for (int i = 0; i < stations.size(); i++) {
                Double meters = row[i];

                // Null betyder, at OSRM ikke fandt en rute
                if (meters != null
                        && Double.isFinite(meters)
                        && meters >= 0) {

                    distances.put(
                            stations.get(i),
                            meters / 1000.0
                    );
                }
            }

        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Kunne ikke hente køreafstande fra OSRM",
                    e
            );
        }
    }

    private record RouteTableResponse(
            String code,
            Double[][] distances
    ) {
    }
}
