package com.smartstay.smartstay.payloads;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record AddHostelPayloads(@NotNull(message = "Hostel name required")
                                @NotEmpty(message = "Hostel name required") String hostelName,
                                @NotNull(message = "Mobile number is required")
                                @NotEmpty(message = "Mobile number is required") String mobile,
                                @NotNull(message = "Pincode is required")
                                @Min(value = 100000, message = "Pincode must be 6 digits")
                                @Max(value = 999999, message = "Pincode must be 6 digits") Integer pincode,
                                @NotNull(message = "City is required")
                                @NotEmpty(message = "City is required")
                                String city,
                                @NotNull(message = "State is required")
                                @NotEmpty(message = "State is required") String state,
                                String emailId, String houseNo, String street, String landmark) {
}
