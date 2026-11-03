package com.example.FuelFinder_Ai.controller;

import com.example.FuelFinder_Ai.DTO.AddressResult;
import com.example.FuelFinder_Ai.service.AddressService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping("/resolve")
    public AddressResult resolve(
            @RequestParam String address
    ) {
        return addressService.resolve(address);
    }
}