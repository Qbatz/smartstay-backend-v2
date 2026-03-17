package com.smartstay.smartstay.controllers;

import com.smartstay.smartstay.payloads.electricity.AddReading;
import com.smartstay.smartstay.payloads.electricity.UpdateElectricity;
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
    public ResponseEntity<?> getAllReadingsNew(@PathVariable("hostelId") String hostelId) {
        return electricityService.getEbReadings(hostelId);
    }

    @GetMapping("/customers/{hostelId}")
    public ResponseEntity<?> getAllCustomersElectricity(@PathVariable("hostelId") String hostelId) {
        return electricityService.getCustomersListElectricity(hostelId);
    }

    @GetMapping("/{hostelId}/{roomId}")
    public ResponseEntity<?> getAllEbReadingsForRoom(@PathVariable("hostelId") String hostelId, @PathVariable("roomId") Integer roomId) {
        return electricityService.getRoomReadingsHistory(hostelId, roomId);
    }

    @GetMapping("/customers/{hostelId}/{customerId}")
    public ResponseEntity<?> getAllEbReadingsByCustomer(@PathVariable("hostelId") String hostelId, @PathVariable("customerId") String customerId) {
        return electricityService.getAllReadingByCustomer(hostelId, customerId);
    }

    @PutMapping("/{hostelId}/{readingId}")
    public ResponseEntity<?> updateEbReading(@PathVariable("hostelId") String hostelId, @PathVariable("readingId") String readingId, UpdateElectricity updateElectricity) {
        return electricityService.updateEBReadings(hostelId, readingId, updateElectricity);
    }

    @DeleteMapping("/{hostelId}/{readingId}")
    public ResponseEntity<?> deleteElectricity(@PathVariable("hostelId") String hostelId, @PathVariable("readingId") String readingId) {
        return electricityService.deleteReading(hostelId, readingId);
    }

    @PostMapping("/calculate/{hostelId}")
    public ResponseEntity<?> calculateEb(@PathVariable("hostelId") String hostelId) {
        return electricityService.calculateEbAmountForCustomers(hostelId);
    }

    @DeleteMapping("/all/{hostelId}")
    public ResponseEntity<?> deleteEntries(@PathVariable("hostelId") String hostelId) {
        return electricityService.deleteReadingAll(hostelId);
    }

}
