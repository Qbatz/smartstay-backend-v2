package com.smartstay.smartstay.payloads.documents;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UploadDocuments(String notes,
                              @NotNull(message = "Document type is required")
                              @NotEmpty(message = "Document type is required")
                              @Pattern(regexp = "^(KYC|CHECKIN|OTHER|kyc|checkin|other)?$", message = "Status must be either 'KYC' or 'CHECKIN' or 'OTHER'")
                              String type) {
}
