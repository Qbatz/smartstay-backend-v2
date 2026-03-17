package com.smartstay.smartstay.payloads.beds;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record EditBedRent(
        @NotNull(message = "Rent amount required")
        @Positive(message = "Invalid rent amount")
        Double newRent){}
