package com.smartstay.smartstay.payloads.asset;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Date;

public record AssetRequest(
        String assetName,
        String productName,

        Integer vendorId,

        String brandName,
        String serialNumber,
        String purchaseDate,
        Double price,
        String bankingId
) {
}
