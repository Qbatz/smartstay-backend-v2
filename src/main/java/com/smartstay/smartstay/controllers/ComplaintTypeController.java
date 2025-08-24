package com.smartstay.smartstay.controllers;

import com.smartstay.smartstay.payloads.complaints.AddComplaintType;
import com.smartstay.smartstay.payloads.complaints.UpdateComplaintType;
import com.smartstay.smartstay.services.ComplaintTypeService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v2/under development1")
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "Authorization")
@CrossOrigin("*")
public class ComplaintTypeController {

    @Autowired
    private ComplaintTypeService complaintTypeService;


    @PostMapping("")
    public ResponseEntity<?> addComplaintsType(@Valid @RequestBody AddComplaintType request) {
        return complaintTypeService.addComplaintType(request);
    }

    @PutMapping("/{complaintTypeId}")
    public ResponseEntity<?> updateComplaintsType(@PathVariable("complaintTypeId") int complaintTypeId, @Valid @RequestBody UpdateComplaintType request) {
        return complaintTypeService.updateComplaintType(request, complaintTypeId);
    }

    @GetMapping("/all-complaintTypes/{hostelId}")
    public ResponseEntity<?> getAllComplaintTypes(@PathVariable("hostelId") String hostelId) {
        return complaintTypeService.getAllComplaintTypes(hostelId);
    }

    @DeleteMapping("/{complaintId}")
    public ResponseEntity<?> deleteComplaintId(@PathVariable("complaintId") int complaintId) {
        return complaintTypeService.deleteComplaintType(complaintId);
    }
}
