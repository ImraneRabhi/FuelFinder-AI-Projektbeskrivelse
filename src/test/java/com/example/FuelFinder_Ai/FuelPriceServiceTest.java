package com.example.FuelFinder_Ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.example.FuelFinder_Ai.DTO.FuelType;
import com.example.FuelFinder_Ai.DTO.Station;
import com.example.FuelFinder_Ai.DTO.fuelprices.FpCompany;
import com.example.FuelFinder_Ai.DTO.fuelprices.FpStationInfo;
import com.example.FuelFinder_Ai.DTO.fuelprices.FpStationResponse;
import com.example.FuelFinder_Ai.client.FuelPricesClient;
import com.example.FuelFinder_Ai.service.FuelPriceService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class FuelPriceServiceTest {

    @Mock
    private FuelPricesClient client;

    @InjectMocks
    private FuelPriceService service;

    private FpStationResponse station(String company, String name, Map<String, String> prices) {
        return new FpStationResponse(
                new FpCompany(1, company),
                new FpStationInfo(1, name, "Vestvejen 95, 6200 Aabenraa",
                        55.04, 9.40, "2026-11-03T11:34:36", "fuel"),
                prices
        );
    }

    @Test
    void returnsOnlyChosenFuelSortedCheapestFirst() {
        when(client.fetchAllStations()).thenReturn(List.of(
                station("OK", "Dyr", Map.of("Blyfri 95 E10", "17.49", "Diesel", "18.10")),
                station("Uno-X", "Billig", Map.of("Blyfri 95 E10", "16.59", "Diesel", "18.89"))
        ));

        List<Station> stations = service.getStations(FuelType.BENZIN95);

        assertEquals(2, stations.size());
        assertEquals("Billig", stations.getFirst().name());
        assertEquals("Uno-X", stations.getFirst().company());
        assertEquals(1, stations.getFirst().prices().size());
        assertEquals("Blyfri 95 E10", stations.getFirst().prices().getFirst().product());
        assertEquals(new BigDecimal("16.59"), stations.getFirst().prices().getFirst().price());
    }

    @Test
    void skipsStationsWithoutChosenFuelAndInvalidPrices() {
        when(client.fetchAllStations()).thenReturn(List.of(
                station("OK", "Kun benzin", Map.of("Blyfri 95 E10", "16.99")),
                station("Shell", "Ugyldig diesel", Map.of("Diesel", "ikke-et-tal")),
                station("Q8", "Gyldig diesel", Map.of("Diesel", "18.89"))
        ));

        List<Station> stations = service.getStations(FuelType.DIESEL);

        assertEquals(1, stations.size());
        assertEquals("Gyldig diesel", stations.getFirst().name());
    }

    @Test
    void givesServiceUnavailableWhenApiFails() {
        when(client.fetchAllStations()).thenThrow(new RestClientException("API nede"));

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.getStations(FuelType.BENZIN95));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, e.getStatusCode());
    }
}