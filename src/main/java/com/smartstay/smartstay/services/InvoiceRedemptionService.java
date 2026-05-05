package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.BillTemplateType;
import com.smartstay.smartstay.dao.BillTemplates;
import com.smartstay.smartstay.dao.InvoiceRedemption;
import com.smartstay.smartstay.ennum.BillConfigTypes;
import com.smartstay.smartstay.ennum.InvoiceType;
import com.smartstay.smartstay.repositories.InvoiceRedemptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class InvoiceRedemptionService {
    @Autowired
    private Authentication authentication;
    @Autowired
    private TemplatesService hostelTemplateService;
    @Autowired
    private InvoiceRedemptionRepository invoiceRedemptionRepository;

    public InvoiceRedemption redeemInvoice(String hostelId, String sourceInvoiceId, String targetInvoiceId, double redemptionAmount, Date redeemedAt, String reason) {
        InvoiceRedemption ir = new InvoiceRedemption();
        ir.setCreatedAt(new Date());
        ir.setRedeemedAt(redeemedAt);
        ir.setSourceInvoiceId(sourceInvoiceId);
        ir.setTargetInvoiceId(targetInvoiceId);
        ir.setRedemptionAmount(redemptionAmount);
        ir.setHostelId(hostelId);
        ir.setTransactionId(getNextReferenceNumber(hostelId));
        ir.setReferenceNumber(null);
        ir.setReason(reason);
        ir.setCreatedBy(authentication.getName());

        return invoiceRedemptionRepository.save(ir);
    }

    private String getNextReferenceNumber(String hostelId) {
        BillTemplates billTemplates = hostelTemplateService.getTemplateByHostelId(hostelId);
        String prefix = "RED";
        StringBuilder prefixSuffix = new StringBuilder();
        if (billTemplates != null) {
            BillTemplateType templateType = billTemplates.getTemplateTypes()
                    .stream()
                    .filter(i -> i.getInvoiceType().equalsIgnoreCase(BillConfigTypes.RENTAL.name()))
                    .findFirst()
                    .orElse(null);
            if (templateType == null) {
                prefixSuffix.append(prefix);
            }
            else {
                prefixSuffix.append(templateType.getInvoicePrefix());
            }
            return getNextInvoiceNumber(hostelId, prefixSuffix.toString());
        }
        return prefix + "-001";
    }

    private String getNextInvoiceNumber(String hostelId, String prefix) {
        InvoiceRedemption ir = invoiceRedemptionRepository.findLatestInvoice(hostelId);
        if (ir ==  null) {
            return prefix + "-001";
        }
        else {
            String[] arrReferenceNUmbers = ir.getReferenceNumber().split("-");
            if (arrReferenceNUmbers.length > 0) {
                String previousPrefix = arrReferenceNUmbers[0];
                String currentPrefix = null;
                if (!previousPrefix.equalsIgnoreCase(prefix)) {
                    return prefix + "-001";
                }
                currentPrefix = previousPrefix;
                String suffix = arrReferenceNUmbers[1];
                int suffixNumbers = Integer.parseInt(suffix);
                if (suffixNumbers < 10) {
                    return currentPrefix + "-00" + suffixNumbers;
                }
                else if (suffixNumbers < 100) {
                    return currentPrefix + "-0" + suffixNumbers;
                }
                else {
                    return currentPrefix + "-" + suffixNumbers;
                }
            }
            else {
                return prefix + "-001";
            }
        }
    }
}
