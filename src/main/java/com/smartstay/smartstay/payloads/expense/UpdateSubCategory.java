package com.smartstay.smartstay.payloads.expense;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record UpdateSubCategory(@NotEmpty(message = "Sub Category name is required")
                                @NotNull(message = "Sub Category name is required")
                                String newSubCategoryName)  {
}
