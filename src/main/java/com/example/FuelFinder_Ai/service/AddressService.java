package com.example.FuelFinder_Ai.service;

import com.example.FuelFinder_Ai.AddressClient;
import com.example.FuelFinder_Ai.DTO.AddressLookupResponse;
import com.example.FuelFinder_Ai.DTO.AddressResult;
import com.example.FuelFinder_Ai.DTO.AddressSearchResponse;
import org.springframework.stereotype.Service;

@Service
public class AddressService {

    private final AddressClient addressClient;
    private final CoordinateService coordinateService;

    public AddressService(
            AddressClient addressClient,
            CoordinateService coordinateService
    ) {
        this.addressClient = addressClient;
        this.coordinateService = coordinateService;
    }

    public AddressResult resolve(String address) {

        // 1. Find adressen og dens ID
        AddressSearchResponse searchResponse =
                addressClient.search(address);

        if (searchResponse == null
                || searchResponse.fund() == null
                || searchResponse.fund().isEmpty()) {

            throw new IllegalArgumentException(
                    "Adressen blev ikke fundet"
            );
        }

        String addressId =
                searchResponse.fund().getFirst().id();

        // 2. Hent adressens x/y-koordinater
        AddressLookupResponse lookupResponse =
                addressClient.findById(addressId);

        if (lookupResponse == null
                || lookupResponse.husnummer() == null
                || lookupResponse.husnummer().adgangspunkt() == null
                || lookupResponse.husnummer()
                .adgangspunkt().koordinater() == null) {

            throw new IllegalArgumentException(
                    "Koordinater blev ikke fundet"
            );
        }

        AddressLookupResponse.Koordinater coordinates =
                lookupResponse.husnummer()
                        .adgangspunkt()
                        .koordinater();

        // 3. Konvertér x/y til latitude/longitude
        return coordinateService.convert(
                lookupResponse.husnummer()
                        .adgangsadressebetegnelse(),
                coordinates.x(),
                coordinates.y()
        );
    }
}