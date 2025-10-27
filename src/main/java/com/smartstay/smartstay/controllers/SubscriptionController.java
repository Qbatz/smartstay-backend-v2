package com.smartstay.smartstay.controllers;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.services.SubscriptionService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v2/subscription")
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "Authorization")
@CrossOrigin("*")
public class SubscriptionController {
    @Autowired
    private SubscriptionService subscriptionService;

    @GetMapping("/{subscriptionId}")
    public ResponseEntity<?> getSubscriptionDetails(@PathVariable("subscriptionId") String subscriptionId) {
        return subscriptionService.getSubscriptionDetails(subscriptionId);
    }
}
