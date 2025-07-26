package com.smartstay.smartstay.controllers;

import com.smartstay.smartstay.services.BookingsService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v2/bookings")
@SecurityScheme(
        name = "Authorization",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
@SecurityRequirement(name = "Authorization")
public class BookingsController {

    @Autowired
    private BookingsService bookingService;

    @GetMapping("/{hostelId}")
    public ResponseEntity<?> getAllBookingsBasedOnHostel(@PathVariable("hostelId") String hostelId) {
        return bookingService.getAllCheckInCustomers(hostelId);
    }
}
