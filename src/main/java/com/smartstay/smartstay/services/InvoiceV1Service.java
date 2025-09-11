package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.BankingListMapper;
import com.smartstay.smartstay.Wrappers.InvoiceListMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.InvoicesV1;
import com.smartstay.smartstay.dao.TransactionV1;
import com.smartstay.smartstay.dto.invoices.Invoices;
import com.smartstay.smartstay.ennum.InvoiceMode;
import com.smartstay.smartstay.ennum.InvoiceType;
import com.smartstay.smartstay.ennum.PaymentStatus;
import com.smartstay.smartstay.repositories.InvoicesV1Repository;
import com.smartstay.smartstay.responses.invoices.InvoicesList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class InvoiceV1Service {
    @Autowired
    InvoicesV1Repository invoicesV1Repository;

    @Autowired
    Authentication authentication;

    @Autowired
    TemplatesService templateService;

    public void addInvoice(String customerId, Double amount, String type, String hostelId) {
        if (authentication.isAuthenticated()) {
            StringBuilder invoiceNumber = new StringBuilder();
            String[] prefixSuffix = templateService.getBillTemplate(hostelId, InvoiceType.ADVANCE.name());
            InvoicesV1 existingV1 = null;
            if (prefixSuffix != null) {
                invoiceNumber.append(prefixSuffix[0]);
                invoiceNumber.append("-");
                invoiceNumber.append(prefixSuffix[1]);
                existingV1 = invoicesV1Repository.findLatestInvoiceByPrefix(prefixSuffix[0]);
            }
            InvoicesV1 invoicesV1 = new InvoicesV1();
            if (existingV1 != null) {
                invoiceNumber = new StringBuilder();
                invoiceNumber.append(prefixSuffix[0]);

                String[] suffix = existingV1.getInvoiceNumber().split("-");
                if (suffix.length > 1) {
                    invoiceNumber.append("-");
                    int suff = Integer.parseInt(suffix[1]) + 1;
                    invoiceNumber.append(String.format("%03d", suff));
                }
            }


            invoicesV1.setAmount(amount);
            invoicesV1.setInvoiceType(type);
            invoicesV1.setCustomerId(customerId);
            invoicesV1.setInvoiceNumber(invoiceNumber.toString());
            invoicesV1.setPaymentStatus(PaymentStatus.PENDING.name());
            invoicesV1.setCreatedBy(authentication.getName());
            invoicesV1.setCreatedAt(new Date());
            invoicesV1.setInvoiceGeneratedDate(new Date());
            invoicesV1.setInvoiceMode(InvoiceMode.MANUAL.name());
            invoicesV1.setHostelId(hostelId);


            invoicesV1Repository.save(invoicesV1);
        }


    }

    public ResponseEntity<?> getTransactions(String hostelId) {
        List<Invoices> listInvoices = invoicesV1Repository.findByHostelId(hostelId);

        List<InvoicesList> invoicesResponse = listInvoices
                .stream()
                .map(item -> new InvoiceListMapper().apply(item))
                .toList();

        return new ResponseEntity<>(invoicesResponse, HttpStatus.OK);
    }
}
