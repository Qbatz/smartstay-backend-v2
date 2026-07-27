package com.smartstay.smartstay.responses.banking;

public record QrBankTypeResponse(
        Integer id,
        String type,
        String name,
        String image,
        String createdAt,
        String updatedAt,
        String createdBy,
        String updatedBy
) {
}
