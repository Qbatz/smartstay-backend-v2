package com.smartstay.smartstay.controllers;

import com.smartstay.smartstay.payloads.rooms.AddRoom;
import com.smartstay.smartstay.payloads.rooms.UpdateRoom;
import com.smartstay.smartstay.services.RoomsService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v2/room")
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "Authorization")
public class RoomsController {

    @Autowired
    private RoomsService roomsService;


    @GetMapping("/all-floors/{floorId}")
    public ResponseEntity<?> getAllRooms(@PathVariable("floorId") int floorId) {
        return roomsService.getAllRooms(floorId);
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<?> getRoomById(@PathVariable("roomId") int roomId) {
        return roomsService.getRoomById(roomId);
    }

    @PostMapping("")
    public ResponseEntity<?> addRoom(@Valid @RequestBody AddRoom roomDto) {
        return roomsService.addRoom(roomDto);
    }

    @PutMapping("/{roomId}")
    public ResponseEntity<?> updateRoomById(@PathVariable("roomId") int roomId, @RequestBody UpdateRoom updateRoom) {
        return roomsService.updateRoomById(roomId, updateRoom);
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<?> deleteRoomById(@PathVariable("roomId") int roomId) {
        return roomsService.deleteRoomById(roomId);
    }
}
