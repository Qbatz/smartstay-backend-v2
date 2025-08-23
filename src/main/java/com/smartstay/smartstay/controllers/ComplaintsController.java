package com.smartstay.smartstay.controllers;


import com.smartstay.smartstay.payloads.complaints.AddComplaints;
import com.smartstay.smartstay.payloads.complaints.UpdateComplaint;
import com.smartstay.smartstay.services.ComplaintsService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v2/under development")
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "Authorization")
@CrossOrigin("*")
public class ComplaintsController {

    @Autowired
    private ComplaintsService complaintsService;

    @PostMapping("")
    public ResponseEntity<?> addComplaints(@Valid @RequestBody AddComplaints complaints) {
        return complaintsService.addComplaints(complaints);
    }

    @PutMapping("/{complaintId}")
    public ResponseEntity<?> updateComplaints(@PathVariable("complaintId") int complaintId, @Valid @RequestBody UpdateComplaint request) {
        return complaintsService.updateComplaints(complaintId, request);
    }

    @GetMapping("/all-complaints/{hostelId}")
    public ResponseEntity<?> getAllComplaints(@PathVariable("hostelId") String hostelId) {
        return complaintsService.getAllComplaints(hostelId);
    }

    @GetMapping("/{complaintId}")
    public ResponseEntity<?> getComplaintsById(@PathVariable("complaintId") int complaintId) {
        return complaintsService.getComplaintById(complaintId);
    }

}
