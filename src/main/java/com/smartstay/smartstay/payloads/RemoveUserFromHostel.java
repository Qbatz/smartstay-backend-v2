package com.smartstay.smartstay.payloads;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record RemoveUserFromHostel(@NotNull(message = "User id required") @NotEmpty(message = "User id required") String userId, @NotEmpty(message = "Hostel id required") @NotNull(message = "Hostel id required") String hostelId) {
}
