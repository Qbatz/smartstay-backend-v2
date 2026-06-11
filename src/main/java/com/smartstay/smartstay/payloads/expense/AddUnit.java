package com.smartstay.smartstay.payloads.expense;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record AddUnit(
        @NotNull(message = "Unit name is required") @NotEmpty(message = "Unit name is required") String unitName) {
}
