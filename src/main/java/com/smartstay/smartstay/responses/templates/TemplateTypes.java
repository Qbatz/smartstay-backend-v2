package com.smartstay.smartstay.responses.templates;

public record TemplateTypes(int typeId,
                            String type,
                            String prefix,
                            String suffix,
                            Double gstPercentile,
                            String selectedBankId,
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
                            String invoiceMailId) {
}
