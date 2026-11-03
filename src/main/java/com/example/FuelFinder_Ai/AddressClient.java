package com.example.FuelFinder_Ai;
import com.example.FuelFinder_Ai.DTO.AddressLookupResponse;
import com.example.FuelFinder_Ai.DTO.AddressSearchResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AddressClient {

    private final RestClient client;

    public AddressClient(RestClient.Builder builder) {
        this.client = builder
                .baseUrl("https://adressevaelger.dk")
                .build();
    }

    public AddressSearchResponse search(String address) {
        return client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/husnumre/soeg")
                        .queryParam("tekst", address)
                        .queryParam("token", "adressevaelger123")
                        .queryParam("maksimum", 10)
                        .build()
                )
                .retrieve()
                .body(AddressSearchResponse.class);
    }
    public AddressLookupResponse findById(String id) {
        return client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/husnumre/{id}")
                        .queryParam("token", "adressevaelger123")
                        .build(id)
                )
                .retrieve()
                .body(AddressLookupResponse.class);
    }
}