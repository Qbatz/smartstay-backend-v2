package com.smartstay.smartstay.controllers;

import com.smartstay.smartstay.services.PlansService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v2/plans")
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "Authorization")
@CrossOrigin("*")
public class PlanController {

    @Autowired
    private PlansService plansService;

    @GetMapping("")
    public ResponseEntity<?> getPlans() {
        return plansService.getAllPlans();
    }

    @GetMapping("/{hostelId}")
    public ResponseEntity<?> getPlanByHostelId(@PathVariable("hostelId") String hostelId) {
        return plansService.getPlanByHostelId(hostelId);
    }
}
