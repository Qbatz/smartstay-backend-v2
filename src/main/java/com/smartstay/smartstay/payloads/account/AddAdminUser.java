package com.smartstay.smartstay.payloads.account;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record AddAdminUser(
        @NotNull(message = "Name cannot be empty")
        @NotEmpty(message = "Name cannot be empty") String name,
        @NotNull(message = "EmailId cannot be empty")
        @NotEmpty(message = "EmailId cannot be empty") String emailId,
        @NotNull(message = "Mobile no cannot be empty")
        @NotEmpty(message = "Mobile no cannot be empty") String mobile,
        @NotNull(message = "Password cannot be empty")
        @NotEmpty(message = "Password cannot be empty") String password,
        @NotNull(message = "role id cannot be empty") int roleId,
        String description) {
}
