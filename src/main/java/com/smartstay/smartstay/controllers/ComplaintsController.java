package com.smartstay.smartstay.controllers;


import com.smartstay.smartstay.payloads.complaints.*;
import com.smartstay.smartstay.services.ComplaintsService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v2/complaint")
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

    @PostMapping("/add-comment/{complaintId}")
    public ResponseEntity<?> addComplaintComments(@PathVariable("complaintId") int complaintId,@Valid @RequestBody AddComplaintComment comment) {
        return complaintsService.addComplaintComments(comment,complaintId);
    }

    @PutMapping("/{complaintId}")
    public ResponseEntity<?> updateComplaints(@PathVariable("complaintId") int complaintId, @Valid @RequestBody UpdateComplaint request) {
        return complaintsService.updateComplaints(complaintId, request);
    }

    @PutMapping("/assign-user/{complaintId}")
    public ResponseEntity<?> assignUser(@PathVariable("complaintId") int complaintId, @Valid @RequestBody AssignUser request) {
        return complaintsService.assignUser(complaintId, request);
    }

    @PutMapping("/update-status/{complaintId}")
    public ResponseEntity<?> updateComplaintStatus(@PathVariable("complaintId") int complaintId, @Valid @RequestBody UpdateStatus request) {
        return complaintsService.updateComplaintStatus(complaintId, request);
    }

    @GetMapping("/all-complaints/{hostelId}")
    public ResponseEntity<?> getAllComplaints(@PathVariable("hostelId") String hostelId,@RequestParam(value = "customerName", required=false) String customerName, @RequestParam(value = "status", required = false) String status,@RequestParam(value = "startDate", required = false) String startDate,@RequestParam(value = "endDate", required = false) String endDate) {
        return complaintsService.getAllComplaints(hostelId,customerName,status,startDate,endDate);
    }

    @GetMapping("/{complaintId}")
    public ResponseEntity<?> getComplaintsById(@PathVariable("complaintId") int complaintId) {
        return complaintsService.getComplaintById(complaintId);
    }

    @DeleteMapping("/delete-complaint/{complaintId}")
    public ResponseEntity<?> deleteComplaint(@PathVariable("complaintId") Integer complaintId) {
        return complaintsService.deleteComplaint(complaintId);
    }


}
