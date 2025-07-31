package com.smartstay.smartstay.controllers;

import com.smartstay.smartstay.payloads.account.AddCustomer;
import com.smartstay.smartstay.payloads.beds.AssignBed;
import com.smartstay.smartstay.payloads.customer.BookingRequest;
import com.smartstay.smartstay.payloads.customer.CheckInRequest;
import com.smartstay.smartstay.services.CustomersService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("v2/customers")
@SecurityScheme(
        name = "Authorization",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
@SecurityRequirement(name = "Authorization")
public class CustomersController {

    @Autowired
    private CustomersService customersService;

    @PostMapping("/check-in")
    public ResponseEntity<?> createCustomer(@RequestPart(value = "profilePic", required = false) MultipartFile file, @Valid @RequestPart CheckInRequest payLoads) {
        return customersService.addCheckIn(file, payLoads);
    }

    @PostMapping("/assign-bed")
    public ResponseEntity<?> assignBed(@Valid @RequestBody AssignBed assignBed) {
        return customersService.assignBed(assignBed);
    }

    @GetMapping("/{hostelId}")
    public ResponseEntity<?> getAllCheckInCustomer(@PathVariable("hostelId") String hostelId) {
        return customersService.getAllCheckInCustomers(hostelId);
    }

    @PostMapping("/add-booking/{hostelId}")
    public ResponseEntity<?> createBooking(@PathVariable("hostelId") String hostelId,@RequestPart(value = "profilePic", required = false) MultipartFile file, @Valid @RequestPart BookingRequest bookingRequest) {
        return customersService.createBooking(file, bookingRequest,hostelId);
    }
 }
