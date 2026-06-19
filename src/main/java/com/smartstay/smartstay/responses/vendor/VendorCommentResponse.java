package com.smartstay.smartstay.responses.vendor;

public record VendorCommentResponse(
        Long id,
        String comment,
        String createdBy,
        String createdAt) {
}
