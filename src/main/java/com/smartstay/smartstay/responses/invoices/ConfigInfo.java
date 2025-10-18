package com.smartstay.smartstay.responses.invoices;

public record ConfigInfo(String termAndCondition,
                         String signatureUrl,
                         String hostelLogo,
                         String address,
                         String templateColor,
                         String invoiceNotes,
                         String invoiceType) {
}
