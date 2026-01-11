package com.smartstay.smartstay.controllers;

import com.smartstay.smartstay.payloads.beds.AssignBed;
import com.smartstay.smartstay.payloads.beds.CancelCheckout;
import com.smartstay.smartstay.payloads.beds.ChangeBed;
import com.smartstay.smartstay.payloads.customer.*;
import com.smartstay.smartstay.services.CustomersService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    @PostMapping("/booking/{hostelId}")
    public ResponseEntity<?> createBooking(@PathVariable("hostelId") String hostelId, @Valid @RequestBody BookingRequest bookingRequest) {
        return customersService.createBooking(bookingRequest,hostelId);
    }

    @PostMapping("/booked/check-in/{customerId}")
    public ResponseEntity<?> checkinExistingCustomer(@PathVariable("customerId") String customerId, @Valid @RequestBody CheckInBookedCustomer checkInBookedCustomer) {
        return customersService.checkinBookedCustomer(customerId, checkInBookedCustomer);
    }

    @PostMapping("/notice/{hostelId}")
    public ResponseEntity<?> moveToNotice(@PathVariable("hostelId") String hostelId, @Valid @RequestBody CheckoutNotice checkoutNotice) {
        return customersService.requestNotice(hostelId, checkoutNotice);
    }
//    @PostMapping("/booked/check-out")
//    public ResponseEntity<?> checkoutExistingCustomer(@Valid @RequestBody CheckoutRequest checkoutRequest) {
//        return customersService.requestCheckout(checkoutRequest);
//    }

    @GetMapping("/{hostelId}")
    public ResponseEntity<?> getAllCustomerForHostel(@PathVariable("hostelId") String hostelId, @RequestParam(value = "name", required=false) String name, @RequestParam(value = "type", required = false) String type) {
        return customersService.getAllCustomersForHostel(hostelId, name, type);
    }

    @GetMapping("/details/{customerId}")
    public ResponseEntity<?> getCustomerDetails(@PathVariable("customerId") String customerId) {
        return customersService.getCustomerDetails(customerId);
    }
    @GetMapping("/settlement/{customerId}")
    public ResponseEntity<?> getFinalSettlementInfo(@PathVariable("customerId") String customerId, @RequestParam(value = "leavingDate", required = false) String leavingDate) {
        return customersService.getInformationForFinalSettlementNew(customerId, leavingDate);
    }
    @PostMapping("/settlement/{customerId}")
    public ResponseEntity<?> generateFinalSettlement(@PathVariable("customerId") String customerId, @RequestBody List<Settlement> deductions) {
        return customersService.generateFinalSettlement(customerId, deductions);
    }
    @PostMapping("/change-bed/{hostelId}/{customerId}")
    public ResponseEntity<?> changeBed(@PathVariable("customerId") String customerId,@PathVariable("hostelId") String hostelId,@Valid @RequestBody ChangeBed request) {
        return customersService.changeBed(hostelId, customerId, request);
    }
    @PostMapping("/cancel-checkout/{hostelId}/{customerId}")
    public ResponseEntity<?> cancelCheckOut(@PathVariable("customerId") String customerId,@PathVariable("hostelId") String hostelId,@Valid @RequestBody CancelCheckout request) {
        return customersService.cancelCheckOut(hostelId, customerId, request);
    }

    @GetMapping("/cancel-checkout/initialize/{hostelId}/{customerId}")
    public ResponseEntity<?> initializeCancelCheckout(@PathVariable("hostelId") String hostelId, @PathVariable("customerId") String customerId) {
        return customersService.initializeCancelCheckout(hostelId, customerId);
    }
    @GetMapping("/checkout/{hostelId}")
    public ResponseEntity<?> checkoutCustomers(@PathVariable("hostelId") String hostelId, @RequestParam(value = "name", required = false) String name) {
        return customersService.getCheckoutCustomers(hostelId, name);
    }
    @DeleteMapping("/{hostelId}/{customerId}")
    public ResponseEntity<?> deleteCustomers(@PathVariable("hostelId") String hostelId, @PathVariable("customerId") String customerId) {
        return customersService.deleteCustomer(hostelId, customerId);
    }
 }
