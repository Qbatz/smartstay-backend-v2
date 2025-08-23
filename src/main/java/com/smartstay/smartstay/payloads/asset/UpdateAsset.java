package com.smartstay.smartstay.payloads.asset;

import java.util.Date;

public record UpdateAsset(
        String assetName,
        String productName,
        String brandName,
        String serialNumber,
        String purchaseDate,
        Double price,
        String modeOfPayment,
        String createdBy,
         Boolean isActive
) {
}
