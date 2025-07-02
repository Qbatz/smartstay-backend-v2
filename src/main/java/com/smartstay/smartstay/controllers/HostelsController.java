package com.smartstay.smartstay.controllers;

import com.smartstay.smartstay.payloads.AddHostelPayloads;
import com.smartstay.smartstay.payloads.RemoveUserFromHostel;
import com.smartstay.smartstay.services.HostelService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("v2/hostel")
@SecurityScheme(
        name = "Authorization",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
@SecurityRequirement(name = "Authorization")
public class HostelsController {

    @Autowired
    HostelService hostelService;

    @PostMapping("")
    public ResponseEntity<?> addHostel(@RequestPart MultipartFile mainImage, @RequestPart List<MultipartFile> additionalImages, @RequestPart AddHostelPayloads payloads) {
        return hostelService.addHostel(mainImage, additionalImages, payloads);
    }

    @GetMapping("")
    public ResponseEntity<?> getAllHostels() {
        return hostelService.fetchAllHostels();
    }

    @DeleteMapping("remove-user")
    public ResponseEntity<?> deletedHostelFromUser(@RequestBody RemoveUserFromHostel removeUserPayload) {
        return hostelService.deleteHostelFromUser(removeUserPayload);
    }

    @DeleteMapping("/{hostelId}")
    public ResponseEntity<?> deleteHostel(@PathVariable("hostelId") String hostelId) {
        return hostelService.deleteHostel(hostelId);
    }


}
