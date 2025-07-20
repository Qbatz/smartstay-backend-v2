package com.smartstay.smartstay.payloads.account;

import jakarta.validation.constraints.*;

public record Login(

        @NotBlank(message = "Email Id is required and cannot be blank")
        @Email(message = "Invalid Email")
        String emailId,

        @NotBlank(message = "Password is required and cannot be blank")
        @Size(min = 8, max = 100, message = "Password must be at least 8 characters and less than 100")
        String password

) {}
