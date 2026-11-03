
package com.example.FuelFinder_Ai.controller;

import com.example.FuelFinder_Ai.DTO.AiRequest;
import com.example.FuelFinder_Ai.service.FuelAiService;
import org.springframework.web.bind.annotation.*;

@RestController
public class FuelAiController {

    private final FuelAiService fuelAiService;

    public FuelAiController(FuelAiService fuelAiService) {
        this.fuelAiService = fuelAiService;
    }

    @PostMapping("/api/fuelfinder/askai")
    public String askai(@RequestBody AiRequest request) {
        return fuelAiService.askAi(
                request.question(),
                request.stationData()
        );
    }
}
