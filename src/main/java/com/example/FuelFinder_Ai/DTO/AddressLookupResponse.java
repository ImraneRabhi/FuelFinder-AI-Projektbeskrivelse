package com.example.FuelFinder_Ai.DTO;

public record AddressLookupResponse(
        String status,
        Husnummer husnummer
) {

    public record Husnummer(
            String adgangsadressebetegnelse,
            Adgangspunkt adgangspunkt
    ) {
    }

    public record Adgangspunkt(
            Koordinater koordinater
    ) {
    }

    public record Koordinater(
            double x,
            double y
    ) {
    }
}