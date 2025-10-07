package com.smartstay.smartstay.controllers;


import com.smartstay.smartstay.payloads.amenity.AmenityRequest;
import com.smartstay.smartstay.payloads.amenity.UpdateStatus;
import com.smartstay.smartstay.services.AmenitiesService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v2/amenity")
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "Authorization")
@CrossOrigin("*")
public class AmentityController {

    @Autowired
    private AmenitiesService amenitiesService;


    @GetMapping("/all-amenities/{hostelId}")
    public ResponseEntity<?> getAllAmenities(@PathVariable("hostelId") String hostelId) {
        return amenitiesService.getAllAmenities(hostelId);
    }

    @GetMapping("/amenity/{hostelId}/{amenityId}")
    public ResponseEntity<?> getAmenitiesById(@PathVariable("hostelId") String hostelId, @PathVariable("amenityId") String amenityId) {
        return amenitiesService.getAmenitiesById(hostelId, amenityId);
    }

    @PostMapping("/{hostelId}")
    public ResponseEntity<?> addAmenity(@Valid @RequestBody AmenityRequest request, @PathVariable("hostelId") String hostelId) {
        return amenitiesService.addAmenity(request, hostelId);
    }

    @PutMapping("/{amenityId}/{hostelId}")
    public ResponseEntity<?> updateAmenity(@RequestBody AmenityRequest request, @PathVariable("amenityId") String amenityId, @PathVariable("hostelId") String hostelId) {
        return amenitiesService.updateAmenity(request, amenityId, hostelId);
    }

    @PutMapping("/assign-amenity/{amenityId}/{hostelId}")
    public ResponseEntity<?> updateStatus(@RequestBody UpdateStatus request, @PathVariable("amenityId") String amenityId, @PathVariable("hostelId") String hostelId) {
        return amenitiesService.updateStatus(request, amenityId, hostelId);
    }

    @DeleteMapping("/{amenityId}/{hostelId}")
    public ResponseEntity<?> deleteAmenityById(@PathVariable("amenityId") String amenityId, @PathVariable("hostelId") String hostelId) {
        return amenitiesService.deleteAmenityById(amenityId, hostelId);
    }


}
