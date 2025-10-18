package com.smartstay.smartstay.dao;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class BillTemplateType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int templateTypeId;
    String invoiceType;
    String invoicePrefix;
    String invoiceSuffix;
    Double gstPercentage;
    Double cgst;
    Double sgst;
    String bankAccountId;
    String qrCode;
    String invoiceNotes;
    String receiptNotes;
    String invoiceTermsAndCondition;
    String receiptTermsAndCondition;
    String invoiceTemplateColor;
    String receiptTemplateColor;
    String receiptLogoUrl;
    String invoiceLogoUrl;
    String receiptPhoneNumber;
    String invoicePhoneNumber;
    String receiptMailId;
    String invoiceMailId;
    String receiptSignatureUrl;
    String invoiceSignatureUrl;

    @ManyToOne
    @JoinColumn(name = "template_id")
    BillTemplates templates;

    @Override
    public String toString() {
        return "BillTemplateType{" +
                "templateTypeId=" + templateTypeId +
                ", invoiceType='" + invoiceType + '\'' +
                ", invoicePrefix='" + invoicePrefix + '\'' +
                ", invoiceSuffix='" + invoiceSuffix + '\'' +
                ", gstPercentage=" + gstPercentage +
                ", cgst=" + cgst +
                ", sgst=" + sgst +
                ", bankAccountId='" + bankAccountId + '\'' +
                ", qrCode='" + qrCode + '\'' +
                ", invoiceNotes='" + invoiceNotes + '\'' +
                ", receiptNotes='" + receiptNotes + '\'' +
                ", invoiceTermsAndCondition='" + invoiceTermsAndCondition + '\'' +
                ", receiptTermsAndCondition='" + receiptTermsAndCondition + '\'' +
                ", invoiceTemplateColor='" + invoiceTemplateColor + '\'' +
                ", receiptTemplateColor='" + receiptTemplateColor + '\'' +
                ", receiptLogoUrl='" + receiptLogoUrl + '\'' +
                ", invoiceLogoUrl='" + invoiceLogoUrl + '\'' +
                ", receiptPhoneNumber='" + receiptPhoneNumber + '\'' +
                ", invoicePhoneNumber='" + invoicePhoneNumber + '\'' +
                ", receiptMailId='" + receiptMailId + '\'' +
                ", invoiceMailId='" + invoiceMailId + '\'' +
                ", receiptSignatureUrl='" + receiptSignatureUrl + '\'' +
                ", invoiceSignatureUrl='" + invoiceSignatureUrl + '\'' +
                ", templates=" + templates +
                '}';
    }
}
