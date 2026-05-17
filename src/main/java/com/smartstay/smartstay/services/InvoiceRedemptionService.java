package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.settlement.RedeemedInvoiceMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.BillTemplateType;
import com.smartstay.smartstay.dao.BillTemplates;
import com.smartstay.smartstay.dao.InvoiceRedemption;
import com.smartstay.smartstay.dao.InvoicesV1;
import com.smartstay.smartstay.dto.invoices.AmountSettled;
import com.smartstay.smartstay.dto.invoices.AppliedInvoices;
import com.smartstay.smartstay.ennum.BillConfigTypes;
import com.smartstay.smartstay.ennum.InvoiceType;
import com.smartstay.smartstay.ennum.UserType;
import com.smartstay.smartstay.payloads.invoice.RedemptionItems;
import com.smartstay.smartstay.repositories.InvoiceRedemptionRepository;
import com.smartstay.smartstay.responses.customer.RedeemedInfo;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class InvoiceRedemptionService {
    @Autowired
    private Authentication authentication;
    @Autowired
    private TemplatesService hostelTemplateService;
    @Autowired
    private InvoiceRedemptionRepository invoiceRedemptionRepository;

    private InvoiceV1Service invoiceV1Service;

    @Autowired
    public void setInvoiceV1Service(@Lazy InvoiceV1Service invoiceV1Service) {
        this.invoiceV1Service = invoiceV1Service;
    }

    public List<InvoiceRedemption> redeemInvoice(String hostelId, String sourceInvoiceId, List<RedemptionItems> targetRedemptions,  Date redeemedAt, String reason) {

        List<InvoiceRedemption> listRedemptions = targetRedemptions
                .stream()
                .map(i -> {
                    InvoiceRedemption ir = new InvoiceRedemption();
                    ir.setCreatedAt(new Date());
                    ir.setRedeemedAt(redeemedAt);
                    ir.setSourceInvoiceId(sourceInvoiceId);
                    ir.setTargetInvoiceId(i.invoiceId());
                    ir.setRedemptionAmount(i.amount());
                    ir.setUserType(UserType.OWNER.name());
                    ir.setIsActive(true);
                    ir.setHostelId(hostelId);
                    ir.setTransactionId(getNextReferenceNumber(hostelId));
                    ir.setReferenceNumber(null);
                    ir.setReason(reason);
                    ir.setCreatedBy(authentication.getName());

                    return ir;
                })
                .toList();



        return invoiceRedemptionRepository.saveAll(listRedemptions);
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
            String[] arrReferenceNUmbers = ir.getTransactionId().split("-");
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

    public List<InvoiceRedemption> getAmountRedeemedForInvoice(String invoiceId, String hostelId) {
        if (!authentication.isAuthenticated()){
            return null;
        }

        return invoiceRedemptionRepository.findByHostelIdAndTargetInvoiceId(hostelId, invoiceId);
    }

    public List<InvoiceRedemption> getRedeemedInvoicesByInvoiceId(String hostelId, List<String> invoicesId) {
        List<InvoiceRedemption> listInvoicesApplied = invoiceRedemptionRepository.findByHostelIdAndTargetInvoiceId(hostelId, invoicesId);
        if (listInvoicesApplied == null) {
            return new ArrayList<>();
        }

        return listInvoicesApplied;
    }

    public List<RedeemedInfo> findRedeemedItemsFromAdvance(String hostelId, String invoiceId) {
        List<InvoiceRedemption> listInvoiceRedemption = invoiceRedemptionRepository.findByHostelIdAndSourceId(hostelId, invoiceId);
        if (listInvoiceRedemption == null) {
            return new ArrayList<>();
        }

        List<String> targetInvoiceIds = listInvoiceRedemption
                .stream()
                .map(InvoiceRedemption::getTargetInvoiceId)
                .toList();

        List<InvoicesV1> listInvoices = invoiceV1Service.findInvoices(targetInvoiceIds);


        return listInvoiceRedemption
                .stream()
                .map(i -> new RedeemedInvoiceMapper(listInvoices).apply(i))
                .toList();
    }

    public double getAdvanceAmountFromBookingInvoice(String hostelId, String invoiceId) {
        List<InvoiceRedemption> listRedeemedInvoices = invoiceRedemptionRepository.findByHostelIdAndTargetId(hostelId, invoiceId);
        if (listRedeemedInvoices == null) {
            return 0.0;
        }

        return listRedeemedInvoices
                .stream()
                .mapToDouble(i -> {
                    if (i.getRedemptionAmount() == null) {
                        return 0.0;
                    }
                    return i.getRedemptionAmount();
                })
                .sum();
    }
}
