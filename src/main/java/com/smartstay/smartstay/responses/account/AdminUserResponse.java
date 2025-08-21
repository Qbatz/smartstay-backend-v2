package com.smartstay.smartstay.responses.account;

public record AdminUserResponse(
        String mobileStatus,
        String emailStatus,
        String message
) {}