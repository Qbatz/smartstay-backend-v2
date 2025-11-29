package com.smartstay.smartstay.controllers;

import com.smartstay.smartstay.payloads.AddHostelPayloads;
import com.smartstay.smartstay.payloads.RemoveUserFromHostel;
import com.smartstay.smartstay.payloads.electricity.UpdateEBConfigs;
import com.smartstay.smartstay.payloads.hostel.BillRules;
import com.smartstay.smartstay.payloads.hostel.UpdateElectricityPrice;
import com.smartstay.smartstay.payloads.hostel.UpdatePg;
import com.smartstay.smartstay.services.HostelService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
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
@CrossOrigin("*")
public class HostelsController {

    @Autowired
    HostelService hostelService;

    @PostMapping("")
    public ResponseEntity<?> addHostel(@RequestPart(required = false, name = "mainImage") MultipartFile mainImage, @RequestPart(required = false, name = "additionalImages") List<MultipartFile> additionalImages, @RequestPart AddHostelPayloads payloads) {
        return hostelService.addHostel(mainImage, additionalImages, payloads);
    }

    @GetMapping("")
    public ResponseEntity<?> getAllHostels() {
        return hostelService.fetchAllHostels();
    }

    @DeleteMapping("/remove-user")
    public ResponseEntity<?> deletedHostelFromUser(@RequestBody RemoveUserFromHostel removeUserPayload) {
        return hostelService.deleteHostelFromUser(removeUserPayload);
    }
    @DeleteMapping("/{hostelId}")
    public ResponseEntity<?> deleteHostel(@PathVariable("hostelId") String hostelId) {
        return hostelService.deleteHostel(hostelId);
    }
    @GetMapping("/{hostelId}")
    public ResponseEntity<?> getHostelDetails(@PathVariable("hostelId") String hostelId) {
        return hostelService.getHostelDetails(hostelId);
    }
    @GetMapping("/free-beds/{hostelId}")
    public ResponseEntity<?> getFreeBeds(@PathVariable("hostelId") String hostelId) {
        return hostelService.findFreeBeds(hostelId);
    }
    @PutMapping("/electricity/{hostelId}")
    public ResponseEntity<?> updateEBUnitPrice(@PathVariable("hostelId") String hostelId, @Valid @RequestBody UpdateElectricityPrice electricityPrice) {
        return hostelService.updateEbPrice(hostelId, electricityPrice);
    }
    @GetMapping("/electricity/{hostelId}")
    public ResponseEntity<?> getEBSettings(@PathVariable("hostelId") String hostelId) {
        return hostelService.getEBSettings(hostelId);
    }
    @PutMapping("/electricity/config/{hostelId}")
    public ResponseEntity<?> updateElectricityConfiguration(@PathVariable("hostelId") String hostelId, UpdateEBConfigs ebConfigs) {
        return hostelService.updateEbConfig(hostelId, ebConfigs);
    }
    @GetMapping("/config/billing/{hostelId}")
    public ResponseEntity<?> viewBillingRules(@PathVariable("hostelId") String hostelId) {
        return hostelService.viewBillingRules(hostelId);
    }
    @PutMapping("/config/billing/{hostelId}")
    public ResponseEntity<?> updateBillingRules(@PathVariable("hostelId") String hostelId, @RequestBody @Valid BillRules billRules) {
        return hostelService.updateBillingRules(hostelId, billRules);
    }

    @PutMapping("/{hostelId}")
    public ResponseEntity<?> updatePGdetails(@PathVariable("hostelId") String hostelId, @RequestPart(required = false) UpdatePg payloads, @RequestPart(required = false, name = "mainImage") MultipartFile mainImage, @RequestPart(required = false, name = "additionalImages") List<MultipartFile> additionalImages) {
        return hostelService.updatePgInformation(hostelId, payloads, mainImage, additionalImages);
    }

}
