package com.smartstay.smartstay.payloads.complaints;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddComplaints(
        @NotBlank(message = "Customer ID is mandatory")
        String customerId,

        @NotNull(message = "Complaint Type ID cannot be null")
        Integer complaintTypeId,

        Integer floorId,
        Integer roomId,
        Integer bedId,

        @NotBlank(message = "Complaint date is mandatory")
        String complaintDate,

        String description,

        @NotBlank(message = "Hostel ID is mandatory")
        String hostelId
) {
}
