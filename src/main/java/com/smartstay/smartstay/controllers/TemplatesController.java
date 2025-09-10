package com.smartstay.smartstay.controllers;

import com.smartstay.smartstay.services.TemplatesService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v2/hostel/config")
@SecurityScheme(
        name = "Authorization",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
@SecurityRequirement(name = "Authorization")
@CrossOrigin("*")
public class TemplatesController {

    @Autowired
    private TemplatesService templateService;

    @GetMapping("/{hostelId}")
    public ResponseEntity<?> getTemplates(@PathVariable("hostelId") String hostelId) {
        return templateService.getTemplates(hostelId);
    }
}
