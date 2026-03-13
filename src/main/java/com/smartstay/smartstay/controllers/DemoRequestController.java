package com.smartstay.smartstay.controllers;

import com.smartstay.smartstay.payloads.demo.DemoRequest;
import com.smartstay.smartstay.services.DemoRequestService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v2/demo")
@CrossOrigin("*")
public class DemoRequestController {

    @Autowired
    private DemoRequestService demoRequestService;

    @PostMapping("/request")
    public ResponseEntity<?> requestDemo(@Valid @RequestBody DemoRequest demoRequest) {
        return demoRequestService.requestDemo(demoRequest);
    }
}
