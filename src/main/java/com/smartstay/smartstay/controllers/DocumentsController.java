package com.smartstay.smartstay.controllers;

import com.smartstay.smartstay.payloads.documents.UploadDocuments;
import com.smartstay.smartstay.services.CustomerDocumentsService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/v2/documents")
@SecurityScheme(
        name = "Authorization",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
@SecurityRequirement(name = "Authorization")
@CrossOrigin("*")
public class DocumentsController {

    @Autowired
    private CustomerDocumentsService customerDocumentsService;

    @PostMapping("/{hostelId}/{customerId}")
    public ResponseEntity<?> addCustomerDocuments(@PathVariable("hostelId") String hostelId,
                                                  @PathVariable("customerId") String customerId,
                                                  @RequestPart(required = true, name = "files") List<MultipartFile> listFiles,
                                                  @RequestPart(name = "payload", required = true) UploadDocuments uploadDocuments) {

        return customerDocumentsService.addFiles(hostelId, customerId, listFiles, uploadDocuments);

    }

    @DeleteMapping("/{hostelId}/{customerId}/{documentId}")
    public ResponseEntity<?> deleteDocuments(@PathVariable("hostelId") String hostelId, @PathVariable("customerId") String customerId, @PathVariable("documentId") String documentId) {
        return customerDocumentsService.deleteDocument(hostelId, customerId, documentId);
    }
}
