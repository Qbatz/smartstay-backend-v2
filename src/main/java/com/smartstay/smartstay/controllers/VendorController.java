package com.smartstay.smartstay.controllers;

import com.smartstay.smartstay.payloads.vendor.AddVendor;
import com.smartstay.smartstay.payloads.vendor.UpdateVendor;
import com.smartstay.smartstay.services.VendorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("v2/vendors")
@CrossOrigin("*")
public class VendorController {

    @Autowired
    VendorService vendorService;

    @GetMapping("/all-vendors/{hostelId}")
    public ResponseEntity<?> getAllVendors(@PathVariable("hostelId") String hostelId) {
        return vendorService.getAllVendors(hostelId);
    }

    @GetMapping("/{vendorId}")
    public ResponseEntity<?> getVendorById(@PathVariable("vendorId") int vendorId) {
        return vendorService.getVendorById(vendorId);
    }

    @PostMapping("")
    public ResponseEntity<?> addVendor(@RequestPart(value = "profilePic", required = false) MultipartFile file, @Valid @RequestPart AddVendor payLoads) {
        return vendorService.addVendor(file, payLoads);
    }

    @PutMapping("/{vendorId}")
    public ResponseEntity<?> updateVendorId(@PathVariable("vendorId") int vendorId, @RequestBody UpdateVendor updateVendor) {
        return vendorService.updateVendorById(vendorId, updateVendor);
    }

    @DeleteMapping("/{vendorId}")
    public ResponseEntity<?> deleteVendorById(@PathVariable("vendorId") int vendorId) {
        return vendorService.deleteVendorById(vendorId);
    }
}
