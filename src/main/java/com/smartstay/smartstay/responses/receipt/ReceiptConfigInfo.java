package com.smartstay.smartstay.responses.receipt;

public record ReceiptConfigInfo(
        String termAndCondition,
        String signatureUrl,
        String hostelLogo,
        String address,
        String templateColor,
        String receiptNotes,
        String receiptType
) {
}
