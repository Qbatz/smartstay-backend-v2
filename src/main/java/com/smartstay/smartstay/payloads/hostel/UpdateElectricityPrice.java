package com.smartstay.smartstay.payloads.hostel;

import jakarta.validation.constraints.NotNull;

public record UpdateElectricityPrice(
        @NotNull(message = "Unit price required")
        Double unitPrice) {
}
