package com.smartstay.smartstay.controllers;

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
@CrossOrigin("*")
public class CustomersController {

    @Autowired
    private CustomersService customersService;

    @PostMapping("/check-in/{customerId}")
    public ResponseEntity<?> createCustomer(@PathVariable("customerId") String customerId, @Valid @RequestBody CheckInRequest payloads) {
        return customersService.addCheckIn(customerId, payloads);
    }

    @PostMapping("/save/{hostelId}")
    public ResponseEntity<?> addCustomer(@PathVariable("hostelId") String hostelId, @Valid @RequestPart(value = "payloads") AddCustomerPartialInfo customerInfo, @RequestPart(value = "profilePic", required = false) MultipartFile file) {
        return customersService.addCustomerPartialInfo(hostelId, customerInfo, file);
    }

    @PutMapping("/update/{customerId}")
    public ResponseEntity<?> updateCustomerInfo(@PathVariable("customerId") String customerId, @RequestPart(value = "payloads", required = false) UpdateCustomerInfo updateInfo, @RequestPart(value = "profilePic", required = false) MultipartFile file) {
        return customersService.updateCustomerInfo(customerId, updateInfo, file);
    }

    @PostMapping("/assign-bed")
    public ResponseEntity<?> assignBed(@Valid @RequestBody AssignBed assignBed) {
        return customersService.assignBed(assignBed);
    }

    @PostMapping("/{hostelId}")
    public ResponseEntity<?> addCustomer(@PathVariable("hostelId") String hostelId, @RequestPart(value = "profilePic", required = false) MultipartFile profilePic, @Valid @RequestPart AddCustomer customerInfo) {
        return customersService.addCustomer(hostelId, profilePic, customerInfo);
    }

//    @GetMapping("/{hostelId}")
//    public ResponseEntity<?> getAllCheckInCustomer(@PathVariable("hostelId") String hostelId) {
//        return customersService.getAllCheckInCustomers(hostelId);
//    }

    @PostMapping("/add-booking/{hostelId}")
    public ResponseEntity<?> createBooking(@PathVariable("hostelId") String hostelId, @Valid @RequestBody BookingRequest bookingRequest) {
        return customersService.createBooking(bookingRequest,hostelId);
    }

    @PostMapping("/check-in")
    public ResponseEntity<?> checkinExistingCustomer(@Valid @RequestBody CheckinCustomer checkinRequest) {
        return customersService.checkinBookedCustomer(checkinRequest);
    }

    @PostMapping("/notice/{hostelId}/{customerId}")
    public ResponseEntity<?> moveToNotice(@PathVariable("customerId") String customerId, @PathVariable("hostelId") String hostelId, CheckoutNotice checkoutNotice) {
        return customersService.requestNotice(customerId, hostelId, checkoutNotice);
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
