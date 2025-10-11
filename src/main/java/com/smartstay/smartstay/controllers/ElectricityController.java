package com.smartstay.smartstay.controllers;

import com.smartstay.smartstay.payloads.electricity.AddReading;
import com.smartstay.smartstay.services.ElectricityService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v2/electricity")
@SecurityScheme(
        name = "Authorization",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
@SecurityRequirement(name = "Authorization")
@CrossOrigin("*")
public class ElectricityController {

    @Autowired
    private ElectricityService electricityService;

    @PostMapping("/{hostelId}")
    public ResponseEntity<?> addMeterReading(@PathVariable("hostelId") String hostelId,  @Valid @RequestBody AddReading readings) {
        return electricityService.addMeterReading(hostelId, readings);
    }

    @GetMapping("/{hostelId}")
    public ResponseEntity<?> getAllReadings(@PathVariable("hostelId") String hostelId) {
        return electricityService.getEBReadings(hostelId);
    }

    @GetMapping("/customers/{hostelId}")
    public ResponseEntity<?> getAllCustomersElectricity(@PathVariable("hostelId") String hostelId) {
        return electricityService.getCustomersListElectricity(hostelId);
    }

    @GetMapping("/{hostelId}/{roomId}")
    public ResponseEntity<?> getAllEbReadingsForRoom(@PathVariable("hostelId") String hostelId, @PathVariable("roomId") Integer roomId) {
        return electricityService.getRoomReadingsHistory(hostelId, roomId);
    }

}
