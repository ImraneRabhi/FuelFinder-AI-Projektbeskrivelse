package com.example.FuelFinder_Ai.controller;

import com.example.FuelFinder_Ai.DTO.FuelType;
import com.example.FuelFinder_Ai.DTO.Station;
import com.example.FuelFinder_Ai.service.FuelPriceService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FuelPriceController {
    private final FuelPriceService service;

    public FuelPriceController(FuelPriceService service) {
        this.service = service;
    }

    @GetMapping("/api/fuelfinder/prices")
    public List<Station> prices(@RequestParam(defaultValue = "benzin95") String fuelType) {
        return service.getStations(FuelType.from(fuelType));
    }
}