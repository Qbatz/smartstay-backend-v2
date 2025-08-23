package com.smartstay.smartstay.payloads.asset;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Date;

public record AssetRequest(
        String assetName,
        String productName,

        @NotNull(message = "Vendor ID must not be null")
        Integer vendorId,

        String brandName,
        String serialNumber,
        String purchaseDate,
        Double price,
        String modeOfPayment,
        String createdBy,

        @NotBlank(message = "Hostel ID must not be empty")
        String hostelId
) {
}
