package com.smartstay.smartstay.payloads;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import jakarta.validation.constraints.*;

public record CreateAccount(

        @NotBlank(message = "First name is required")
        @Size(min = 2, max = 30, message = "First name must be between 2 and 30 characters")
        String firstName,

        @Size(max = 30, message = "Last name must be at most 30 characters")
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid Email")
        @Size(min = 5, max = 100, message = "Invalid EmailID")
        String mailId,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be at least 8 characters and less than 100")
        String password,

        @NotBlank(message = "Confirm password is required")
        @Size(min = 8, max = 100, message = "Confirm Password must be at least 8 characters and less than 100")
        String confirmPassword,

        @NotBlank(message = "Mobile number is required")
        @Size(min = 10, max = 10, message = "Invalid MobileNumber")
        String mobile

) {}
