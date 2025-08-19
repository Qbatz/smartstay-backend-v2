package com.smartstay.smartstay.payloads.asset;

import java.util.Date;

public record AssetRequest(
        String assetName,
        String productName,
        String vendorId,
        String brandName,
        String serialNumber,
        Date purchaseDate,
        Double price,
        String modeOfPayment,
        String createdBy,
        String hostelId,
        String parentId
) {
}
