package com.smartstay.smartstay.payloads.beds;

import jakarta.validation.constraints.Positive;

public record UpdateBed(
        String bedName,
        Boolean isActive,
        Double amount
) {
}
