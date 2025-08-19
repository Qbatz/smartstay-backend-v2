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
@CrossOrigin("*")
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

    @DeleteMapping("/{floorId}")
    public ResponseEntity<?> deleteFloorById(@PathVariable("floorId") int floorId) {
        return floorsService.deleteFloorById(floorId);
    }

}
