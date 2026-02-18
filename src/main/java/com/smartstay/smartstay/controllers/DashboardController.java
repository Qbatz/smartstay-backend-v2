package com.smartstay.smartstay.controllers;

import com.smartstay.smartstay.services.DashboardService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v2/dashboard")
@SecurityScheme(
        name = "Authorization",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
@SecurityRequirement(name = "Authorization")
@CrossOrigin("*")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/{hostelId}")
    public ResponseEntity<?> getDashboardDate(@PathVariable("hostelId") String hostelId) {
        return dashboardService.getDashboardInfo(hostelId);
    }

    @GetMapping("/new/{hostelId}")
    public ResponseEntity<?> getDashboardDataNew(@PathVariable("hostelId") String hostelId) {
        return dashboardService.getDashboardInfoNew(hostelId);
    }
}
