package com.smartstay.smartstay.controllers;

import com.smartstay.smartstay.payloads.account.AddCustomer;
import com.smartstay.smartstay.payloads.beds.AssignBed;
import com.smartstay.smartstay.payloads.customer.*;
import com.smartstay.smartstay.services.CustomersService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
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

//    @GetMapping("/{hostelId}")
//    public ResponseEntity<?> getAllCheckInCustomer(@PathVariable("hostelId") String hostelId) {
//        return customersService.getAllCheckInCustomers(hostelId);
//    }

    @PostMapping("/add-booking/{hostelId}")
    public ResponseEntity<?> createBooking(@PathVariable("hostelId") String hostelId,@RequestPart(value = "profilePic", required = false) MultipartFile file, @Valid @RequestPart BookingRequest bookingRequest) {
        return customersService.createBooking(file, bookingRequest,hostelId);
    }

    @PostMapping("/booked/check-in")
    public ResponseEntity<?> checkinExistingCustomer(@Valid @RequestBody CheckinCustomer checkinRequest) {
        return customersService.checkinBookedCustomer(checkinRequest);
    }

//    @PostMapping("/booked/check-out")
//    public ResponseEntity<?> checkoutExistingCustomer(@Valid @RequestBody CheckoutRequest checkoutRequest) {
//        return customersService.requestCheckout(checkoutRequest);
//    }

    @GetMapping("/{hostelId}")
    public ResponseEntity<?> getAllCustomerForHostel(@PathVariable("hostelId") String hostelId, @RequestParam(value = "name", required=false) String name, @RequestParam(value = "type", required = false) String type) {
        return customersService.getAllCustomersForHostel(hostelId, name, type);
    }
 }
