package com.smartstay.smartstay.Wrappers.Bills;

import com.smartstay.smartstay.dao.BillTemplateType;
import com.smartstay.smartstay.ennum.BillConfigTypes;
import com.smartstay.smartstay.responses.templates.TemplateTypes;

import java.util.function.Function;

public class TemplateTypeMapper implements Function<BillTemplateType, TemplateTypes> {
    @Override
    public TemplateTypes apply(BillTemplateType billTemplateType) {
        String templateTypes = null;
        if (billTemplateType.getInvoiceType().equalsIgnoreCase(BillConfigTypes.ADVANCE.name())) {
            templateTypes = "Security Deposit";
        }
        else if (billTemplateType.getInvoiceType().equalsIgnoreCase(BillConfigTypes.RENTAL.name())) {
            templateTypes = "Rental";
        }
        return new TemplateTypes(billTemplateType.getTemplateTypeId(),
                templateTypes,
                billTemplateType.getInvoicePrefix(),
                billTemplateType.getInvoiceSuffix(),
                billTemplateType.getGstPercentage(),
                billTemplateType.getBankAccountId(),
                billTemplateType.getQrCode(),
                billTemplateType.getInvoiceNotes(),
                billTemplateType.getReceiptNotes(),
                billTemplateType.getInvoiceTermsAndCondition(),
                billTemplateType.getReceiptTermsAndCondition(),
                billTemplateType.getInvoiceTemplateColor(),
                billTemplateType.getReceiptTemplateColor(),
                billTemplateType.getReceiptLogoUrl(),
                billTemplateType.getReceiptSignatureUrl(),
                billTemplateType.getInvoiceLogoUrl(),
                billTemplateType.getInvoiceSignatureUrl(),
                billTemplateType.getReceiptPhoneNumber(),
                billTemplateType.getInvoicePhoneNumber(),
                billTemplateType.getReceiptMailId(),
                billTemplateType.getInvoiceMailId());
    }
}
