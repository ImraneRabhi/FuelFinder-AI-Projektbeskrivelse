
package com.example.FuelFinder_Ai.service;

import com.example.FuelFinder_Ai.DTO.Coordinates;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class GeocodingService {

    private final RestClient client;

    public GeocodingService(RestClient.Builder builder) {
        this.client = builder
                .baseUrl("https://nominatim.openstreetmap.org")
                .defaultHeader(
                        "User-Agent",
                        "FuelFinderAI/1.0 (contact: imranerabhi77@gmail.com)"
                )
                .build();
    }

    public Coordinates findCoordinates(String location) {

        if (location == null || location.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Du skal indtaste en adresse, by eller et område."
            );
        }

        String searchLocation = location.trim();

        if (!searchLocation.toLowerCase(Locale.ROOT)
                .contains("danmark")) {
            searchLocation += ", Danmark";
        }

        final String finalSearchLocation = searchLocation;

        try {
            GeoResult[] results = client.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search")
                            .queryParam("q", finalSearchLocation)
                            .queryParam("format", "json")
                            .queryParam("limit", 1)
                            .build())
                    .retrieve()
                    .body(GeoResult[].class);

            if (results == null || results.length == 0) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Kunne ikke finde området: " + location
                );
            }

            return new Coordinates(
                    Double.parseDouble(results[0].lat()),
                    Double.parseDouble(results[0].lon())
            );

        } catch (HttpClientErrorException.Forbidden e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Geocoding-tjenesten afviste adgangen (403). " +
                            "Kontrollér User-Agent og API-reglerne.",
                    e
            );
        }
    }

    public record GeoResult(
            String lat,
            String lon
    ) {
    }
}
