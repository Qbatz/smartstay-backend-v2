package com.smartstay.smartstay.payloads.billTemplate;

import org.springframework.web.multipart.MultipartFile;

public record UpdateBillTemplate(
        Integer templateTypeId,
        String prefix,
        String suffix,
        String gstPercentile,
        String bankId,
        String invoiceNotes,
        String receiptNotes,
        String invoiceTermsAndCondition,
        String receiptTermsAndCondition,
        String invoiceTemplateColor,
        String receiptTemplateColor,
        String invoicePhoneNumber,
        String receiptPhoneNumber,
        String invoiceMailId,
        String receiptMailId,
        MultipartFile signature
) {
}
