package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.InvoicesV1;
import com.smartstay.smartstay.dao.TransactionV1;
import com.smartstay.smartstay.ennum.InvoiceMode;
import com.smartstay.smartstay.ennum.PaymentStatus;
import com.smartstay.smartstay.repositories.InvoicesV1Repository;
import org.springframework.beans.factory.annotation.Autowired;
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

    public void addInvoice(String customerId, Double amount, String type, String hostelId) {
        if (authentication.isAuthenticated()) {
            InvoicesV1 invoicesV1 = new InvoicesV1();
            invoicesV1.setAmount(amount);
            invoicesV1.setInvoiceType(type);
            invoicesV1.setCustomerId(customerId);
            invoicesV1.setPaymentStatus(PaymentStatus.PENDING.name());
            invoicesV1.setCreatedBy(authentication.getName());
            invoicesV1.setCreatedAt(new Date());
            invoicesV1.setInvoicGeneratedDate(new Date());
            invoicesV1.setInvoiceMode(InvoiceMode.MANUAL.name());
            invoicesV1.setHostelId(hostelId);


            invoicesV1Repository.save(invoicesV1);
        }


    }

    public ResponseEntity<?> getTransactions(String hostelId) {
        List<InvoicesV1> listInvoices = invoicesV1Repository.findByHostelId(hostelId);

        return new ResponseEntity<>(listInvoices, HttpStatus.OK);
    }
}
