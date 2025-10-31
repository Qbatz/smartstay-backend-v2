package com.smartstay.smartstay.controllers;

import com.smartstay.smartstay.services.SubscriptionService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v2/subscription")
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "Authorization")
@CrossOrigin("*")
public class SubscriptionController {
    @Autowired
    private SubscriptionService subscriptionService;


    @GetMapping("/{hostelId}")
    public ResponseEntity<?> getCurrentPlan(@PathVariable("hostelId") String hostelId) {
        return subscriptionService.getCurrentPlan(hostelId);
    }
}
