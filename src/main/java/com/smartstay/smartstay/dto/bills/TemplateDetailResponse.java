package com.smartstay.smartstay.dto.bills;

public record TemplateDetailResponse(
        Integer typeId,
        String type,
        String prefix,
        String suffix,
        Double gstPercentile,
        String selectedBankId,
        String accountNumber,
        String bankName,
        String ifscCode,

        String upiId,
        String qrCodeUrl,
        String invoiceNotes,
        String receiptNotes,
        String invoiceTermsAndCondition,
        String receiptTermsAndCondition,
        String invoiceTemplateColor,
        String receiptTemplateColor,
        String receiptLogoUrl,
        String receiptSignatureUrl,
        String invoiceLogoUrl,
        String invoiceSignatureUrl,
        String receiptMobileNumber,
        String invoiceMobileNumber,
        String receiptMailId,
        String invoiceMailId
) { }
