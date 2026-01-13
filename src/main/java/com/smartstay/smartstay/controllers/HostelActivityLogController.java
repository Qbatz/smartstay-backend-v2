package com.smartstay.smartstay.controllers;

import com.smartstay.smartstay.services.HostelActivityLogService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v2/logs")
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "Authorization")
@CrossOrigin("*")
public class HostelActivityLogController {

    @Autowired
    private HostelActivityLogService hostelActivityLogService;

    @GetMapping("/{hostelId}")
    public ResponseEntity<?> getActivityLogs(
            @PathVariable("hostelId") String hostelId,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return hostelActivityLogService.getActivityLogs(hostelId, search, page, size);
    }

}
