package com.smartstay.smartstay.controllers;

import com.smartstay.smartstay.payloads.expense.SettleVendorPayment;
import com.smartstay.smartstay.payloads.vendor.AddVendor;
import com.smartstay.smartstay.payloads.vendor.AddVendorCategory;
import com.smartstay.smartstay.payloads.vendor.UpdateVendorCategory;
import com.smartstay.smartstay.payloads.vendor.AddVendorComment;
import com.smartstay.smartstay.payloads.vendor.UpdateVendor;
import com.smartstay.smartstay.payloads.vendor.UpdateVendorComment;
import com.smartstay.smartstay.services.ExpenseService;
import com.smartstay.smartstay.services.VendorCommentService;
import com.smartstay.smartstay.services.VendorService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("v2/vendors")
@SecurityScheme(
        name = "Authorization",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
@SecurityRequirement(name = "Authorization")
@CrossOrigin("*")
public class VendorController {

    @Autowired
    VendorService vendorService;

    @Autowired
    ExpenseService expenseService;

    @Autowired
    VendorCommentService vendorCommentService;

    @PostMapping("/comments")
    public ResponseEntity<?> addVendorComment(@Valid @RequestBody AddVendorComment payLoads) {
        return vendorCommentService.addComment(payLoads);
    }

    @GetMapping("/comments/{vendorId}")
    public ResponseEntity<?> getVendorComments(@PathVariable("vendorId") int vendorId,
                                               @RequestParam(value = "page", defaultValue = "1") int page,
                                               @RequestParam(value = "size", defaultValue = "10") int size) {
        return vendorCommentService.getComments(vendorId, page, size);
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<?> updateVendorComment(@PathVariable("commentId") Long commentId,
                                                 @Valid @RequestBody UpdateVendorComment payLoads) {
        return vendorCommentService.updateComment(commentId, payLoads);
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<?> deleteVendorComment(@PathVariable("commentId") Long commentId) {
        return vendorCommentService.deleteComment(commentId);
    }

    @PostMapping("/settle/{vendorId}")
    public ResponseEntity<?> settleVendorExpenses(@PathVariable("vendorId") String vendorId,
                                                  @RequestPart(value = "images", required = false) MultipartFile[] images,
                                                  @Valid @RequestPart SettleVendorPayment payLoads) {
        return expenseService.settleVendorExpenses(vendorId, images, payLoads);
    }

    @GetMapping("/initialize/{hostelId}/{vendorId}")
    public ResponseEntity<?> initializeVendorSettlement(@PathVariable("hostelId") String hostelId,
                                                        @PathVariable("vendorId") int vendorId) {
        return expenseService.initializeVendorSettlement(hostelId, vendorId);
    }

    @GetMapping("/all-vendors/{hostelId}")
    public ResponseEntity<?> getAllVendors(@PathVariable("hostelId") String hostelId,
                                           @RequestParam(value = "name", required = false) String name,
                                           @RequestParam(value = "categoryId", required = false) Integer categoryId,
                                           @RequestParam(value = "paymentStatus", required = false) List<String> paymentStatus,
                                           @RequestParam(value = "page", defaultValue = "1") int page,
                                           @RequestParam(value = "size", defaultValue = "10") int size) {
        return vendorService.getAllVendors(hostelId, name, categoryId, paymentStatus, page, size);
    }

    // Lightweight, unpaginated lookup of active vendors (id, name, business name) for a hostel.
    @GetMapping("/hostel/{hostelId}/vendors")
    public ResponseEntity<?> getVendorLookupByHostel(@PathVariable("hostelId") String hostelId) {
        return vendorService.getVendorLookupByHostel(hostelId);
    }

    @GetMapping("/{vendorId}")
    public ResponseEntity<?> getVendorById(@PathVariable("vendorId") int vendorId,
                                           @RequestParam(value = "period", required = false) String period) {
        return vendorService.getVendorById(vendorId, period);
    }

    @GetMapping("/expenses/{vendorId}")
    public ResponseEntity<?> getVendorExpenses(@PathVariable("vendorId") int vendorId,
                                               @RequestParam(value = "search", required = false) String search,
                                               @RequestParam(value = "startDate", required = false) String startDate,
                                               @RequestParam(value = "endDate", required = false) String endDate,
                                               @RequestParam(value = "page", defaultValue = "1") int page,
                                               @RequestParam(value = "size", defaultValue = "10") int size) {
        return vendorService.getVendorExpenses(vendorId, search, startDate, endDate, page, size);
    }

    @GetMapping("/expense-payments/{vendorId}")
    public ResponseEntity<?> getVendorExpensePayments(@PathVariable("vendorId") int vendorId,
                                                      @RequestParam(value = "startDate", required = false) String startDate,
                                                      @RequestParam(value = "endDate", required = false) String endDate,
                                                      @RequestParam(value = "page", defaultValue = "1") int page,
                                                      @RequestParam(value = "size", defaultValue = "10") int size) {
        return vendorService.getVendorExpensePayments(vendorId, startDate, endDate, page, size);
    }

    @PostMapping("")
    public ResponseEntity<?> addVendor(@RequestPart(value = "profilePic", required = false) MultipartFile file, @Valid @RequestPart AddVendor payLoads) {
        return vendorService.addVendor(file, payLoads);
    }

    @PutMapping("/{vendorId}")
    public ResponseEntity<?> updateVendorId(@PathVariable("vendorId") int vendorId, @Valid @RequestPart UpdateVendor updateVendor,@RequestPart(value = "profilePic", required = false) MultipartFile file) {
        return vendorService.updateVendorById(vendorId, updateVendor,file);
    }

    @DeleteMapping("/{vendorId}")
    public ResponseEntity<?> deleteVendorById(@PathVariable("vendorId") int vendorId) {
        return vendorService.deleteVendorById(vendorId);
    }

    @PostMapping("/categories")
    public ResponseEntity<?> addVendorCategory(@Valid @RequestBody AddVendorCategory payLoads) {
        return vendorService.addVendorCategory(payLoads);
    }

    @GetMapping("/categories")
    public ResponseEntity<?> getAllVendorCategories(@RequestParam("hostelId") String hostelId) {
        return vendorService.getAllVendorCategories(hostelId);
    }

    // Rename a vendor category (name only); ownership is verified against the supplied hostelId.
    @PutMapping("/categories/{categoryId}")
    public ResponseEntity<?> updateVendorCategory(@PathVariable("categoryId") int categoryId,
                                                  @RequestParam("hostelId") String hostelId,
                                                  @Valid @RequestBody UpdateVendorCategory payLoads) {
        return vendorService.updateVendorCategory(categoryId, hostelId, payLoads);
    }

    @PostMapping("/categories/{categoryId}/delete")
    public ResponseEntity<?> deleteVendorCategory(@PathVariable("categoryId") int categoryId, @RequestParam("hostelId") String hostelId) {
        return vendorService.deleteVendorCategory(categoryId, hostelId);
    }
}
