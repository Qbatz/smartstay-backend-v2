package com.smartstay.smartstay.payloads.complaints;

import jakarta.validation.constraints.NotBlank;

public record UpdateComplaintType(

        @NotBlank(message = "ComplaintTypeName cannot be blank")
        String complaintTypeName,

        @NotBlank(message = "HostelId is required")
        String hostelId,
        Boolean isActive
) {
}
