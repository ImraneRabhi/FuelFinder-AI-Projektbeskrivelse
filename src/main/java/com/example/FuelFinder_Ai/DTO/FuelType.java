package com.example.FuelFinder_Ai.DTO;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public enum FuelType {
    BENZIN95,
    DIESEL;

    public boolean matches(String product) {
        if (product == null) {
            return false;
        }
        String p = product.toLowerCase();
        boolean premium = p.contains("v-power") || p.contains("extra")
                || p.contains("premium") || p.contains("100");
        if (premium) {
            return false;
        }
        return switch (this) {
            case BENZIN95 -> p.contains("95") && !p.contains("diesel");
            case DIESEL -> p.contains("diesel");
        };
    }

    public static FuelType from(String value) {
        return switch (value.trim().toLowerCase()) {
            case "benzin95", "benzin", "95" -> BENZIN95;
            case "diesel" -> DIESEL;
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Ugyldigt brændstof. Vælg 'benzin95' eller 'diesel'.");
        };
    }
}