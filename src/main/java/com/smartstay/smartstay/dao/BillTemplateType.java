package com.smartstay.smartstay.dao;

import jakarta.persistence.*;
import lombok.*;

@Data
@Getter
@Setter
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
}
