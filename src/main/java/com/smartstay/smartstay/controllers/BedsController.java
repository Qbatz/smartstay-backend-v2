package com.smartstay.smartstay.controllers;

import com.smartstay.smartstay.payloads.beds.AddBed;
import com.smartstay.smartstay.payloads.beds.UpdateBed;
import com.smartstay.smartstay.services.BedsService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v2/bed")
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "Authorization")
@CrossOrigin("*")
public class BedsController {


    @Autowired
    private BedsService bedsService;


    @GetMapping("/all-beds/{roomId}")
    public ResponseEntity<?> getAllBeds(@PathVariable("roomId") int roomId) {
        return bedsService.getAllBeds(roomId);
    }

    @GetMapping("/{bedId}")
    public ResponseEntity<?> getBedById(@PathVariable("bedId") int bedId) {
        return bedsService.getBedById(bedId);
    }

    @PostMapping("")
    public ResponseEntity<?> addBed(@Valid @RequestBody AddBed bedDto) {
        return bedsService.addBed(bedDto);
    }

    @PutMapping("/{bedId}")
    public ResponseEntity<?> updateBedById(@PathVariable("bedId") int bedId, @RequestBody UpdateBed updateBed) {
        return bedsService.updateBedById(bedId, updateBed);
    }

    @DeleteMapping("/{bedId}")
    public ResponseEntity<?> deleteBedById(@PathVariable("bedId") int bedId) {
        return bedsService.deleteBedById(bedId);
    }
    @GetMapping("/initialize/{hostelId}")
    public ResponseEntity<?> initializeBooking(@PathVariable("hostelId") String hostelId, @RequestParam("joiningDate") String joiningDate) {
        return bedsService.initializeBooking(hostelId, joiningDate);
    }

}
