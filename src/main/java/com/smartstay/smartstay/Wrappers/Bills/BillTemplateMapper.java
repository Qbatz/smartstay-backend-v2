package com.smartstay.smartstay.Wrappers.Bills;

import com.smartstay.smartstay.dao.BankingV1;
import com.smartstay.smartstay.dao.BillTemplates;
import com.smartstay.smartstay.dto.bills.BillTemplateResponse;
import com.smartstay.smartstay.dto.bills.TemplateDetailResponse;
import com.smartstay.smartstay.repositories.BankingRepository;

import java.util.List;
import java.util.stream.Collectors;

public class BillTemplateMapper {

    private final BankingRepository bankingRepository;

    public BillTemplateMapper(BankingRepository bankingRepository) {
        this.bankingRepository = bankingRepository;
    }

    public BillTemplateResponse toResponse(BillTemplates billTemplate) {
        List<TemplateDetailResponse> templateResponses = billTemplate.getTemplateTypes()
                .stream()
                .map(type -> {
                    BankingV1 bank = null;
                    if (type.getBankAccountId() != null) {
                        bank = bankingRepository.findById(type.getBankAccountId()).orElse(null);
                    }

                    return new TemplateDetailResponse(
                            type.getTemplateTypeId(),
                            type.getInvoiceType(),
                            type.getInvoicePrefix(),
                            type.getInvoiceSuffix(),
                            type.getGstPercentage(),
                            type.getBankAccountId(),
                            bank != null ? bank.getAccountNumber() : null,
                            bank != null ? bank.getBankName() : null,
                            bank != null ? bank.getIfscCode() : null,
                            bank != null ? bank.getUpiId() : null,
                            type.getQrCode(),
                            type.getInvoiceNotes(),
                            type.getReceiptNotes(),
                            type.getInvoiceTermsAndCondition(),
                            type.getReceiptTermsAndCondition(),
                            type.getInvoiceTemplateColor(),
                            type.getReceiptTemplateColor(),
                            type.getReceiptLogoUrl(),
                            type.getReceiptSignatureUrl(),
                            type.getInvoiceLogoUrl(),
                            type.getInvoiceSignatureUrl(),
                            type.getReceiptPhoneNumber(),
                            type.getInvoicePhoneNumber(),
                            type.getReceiptMailId(),
                            type.getInvoiceMailId()
                    );
                })
                .collect(Collectors.toList());

        return new BillTemplateResponse(
                billTemplate.getTemplateId(),
                billTemplate.getHostelId(),
                billTemplate.getDigitalSignature(),
                billTemplate.getHostelLogo(),
                billTemplate.getEmailId(),
                billTemplate.getMobile(),
                billTemplate.getCreatedAt().toString(),
                billTemplate.isLogoCustomized(),
                billTemplate.isMobileCustomized(),
                billTemplate.isEmailCustomized(),
                billTemplate.isSignatureCustomized(),
                templateResponses
        );
    }
}
