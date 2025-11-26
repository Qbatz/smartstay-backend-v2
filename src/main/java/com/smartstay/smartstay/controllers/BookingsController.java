package com.smartstay.smartstay.controllers;

import com.smartstay.smartstay.payloads.booking.CancelBooking;
import com.smartstay.smartstay.payloads.booking.UpdateBookingDetails;
import com.smartstay.smartstay.services.BookingsService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v2/bookings")
@SecurityScheme(
        name = "Authorization",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
@SecurityRequirement(name = "Authorization")
@CrossOrigin("*")
public class BookingsController {

    @Autowired
    private BookingsService bookingService;

    @GetMapping("/{hostelId}")
    public ResponseEntity<?> getAllBookingsBasedOnHostel(@PathVariable("hostelId") String hostelId) {
        return bookingService.getAllCheckInCustomers(hostelId);
    }
    @GetMapping("/initialize-check-in/{hostelId}/{customerId}")
    public ResponseEntity<?> initializeCheckIn(@PathVariable("hostelId") String hostelId, @PathVariable("customerId") String customerId) {
        return bookingService.initializeCheckIn(hostelId, customerId);
    }
    @PutMapping("/cancel/{customerId}")
    public ResponseEntity<?> cancelBooking(@PathVariable("customerId") String customerId, @RequestBody @Valid  CancelBooking cancelBooking) {
        return bookingService.cancelBooking(customerId, cancelBooking);
    }
    @GetMapping("/initialize/cancel/{customerId}")
    public ResponseEntity<?> initiateCancel(@PathVariable("customerId") String customerId) {
        return bookingService.initiateCancel(customerId);
    }
    @PostMapping("/checkout/{customerId}")
    public ResponseEntity<?> initializeCheckout(@PathVariable("customerId") String customerId) {
        return bookingService.checkoutCustomer(customerId);
    }

    @PutMapping("/rent/{hostelId}/{bookingId}")
    public ResponseEntity<?> updateBookingInformations(@PathVariable("hostelId") String hostelId, @PathVariable("bookingId") String bookingId, UpdateBookingDetails updateInfo) {
        return bookingService.updateBookingInfo(hostelId, bookingId, updateInfo);
    }

    @PutMapping("/{hostelId}/{bookingId}")
    public ResponseEntity<?> updateBookingInformationsNew(@PathVariable("hostelId") String hostelId, @PathVariable("bookingId") String bookingId, UpdateBookingDetails updateInfo) {
        return bookingService.updateBookingInfo(hostelId, bookingId, updateInfo);
    }


}
