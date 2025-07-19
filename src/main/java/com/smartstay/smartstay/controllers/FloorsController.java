package com.smartstay.smartstay.controllers;

import com.smartstay.smartstay.payloads.floor.AddFloors;
import com.smartstay.smartstay.payloads.floor.UpdateFloor;
import com.smartstay.smartstay.payloads.rooms.AddRoom;
import com.smartstay.smartstay.payloads.rooms.UpdateRoom;
import com.smartstay.smartstay.services.FloorsService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v2/floor")
@SecurityScheme(
        name = "Authorization",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
@SecurityRequirement(name = "Authorization")
public class FloorsController {


    @Autowired
    private FloorsService floorsService;


    @GetMapping("/all-floors/{hostelId}")
    public ResponseEntity<?> getAllFloors(@PathVariable("hostelId") String hostelId) {
        return floorsService.getAllFloors(hostelId);
    }

    @GetMapping("/{floorId}")
    public ResponseEntity<?> getFloorById(@PathVariable("floorId") int floorId) {
        return floorsService.getFloorById(floorId);
    }

    @PostMapping("")
    public ResponseEntity<?> addFloor(@Valid @RequestBody AddFloors floors) {
        return floorsService.addFloor(floors);
    }

    @PutMapping("/{floorId}")
    public ResponseEntity<?> updateFloorById(@PathVariable("floorId") int floorId, @RequestBody UpdateFloor updateFloor) {
        return floorsService.updateFloorById(floorId, updateFloor);
    }

    @DeleteMapping("remove-floor/{floorId}")
    public ResponseEntity<?> deleteFloorById(@PathVariable("floorId") int floorId) {
        return floorsService.deleteFloorById(floorId);
    }
    //'1', '1', '629162', '2025-07-18 16:41:20.430000', '2025-07-18 16:41:20.430000', 'Irenipuram', '155ce2b2-6774-4463-b42d-57399f31bebb', NULL, '5024a037-62d7-4a87-afdc-6c0c05128acf', 'James', NULL, NULL, 'https://smartstaydevs.s3.ap-south-1.amazonaws.com/Hostel-Images/hostel-building.jpg', '7022736579', '4c22a1ad-d24e-4c18-9ffa-4932f75e0e83', 'Tamil Nadu', NULL
}
