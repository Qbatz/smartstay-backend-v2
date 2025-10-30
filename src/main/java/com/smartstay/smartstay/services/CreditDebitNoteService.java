package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.CreditDebitNotes;
import com.smartstay.smartstay.dao.InvoicesV1;
import com.smartstay.smartstay.dto.customer.CancelBookingDto;
import com.smartstay.smartstay.ennum.CreditDebitNoteSource;
import com.smartstay.smartstay.ennum.CreditDebitNotesType;
import com.smartstay.smartstay.ennum.InvoiceType;
import com.smartstay.smartstay.payloads.invoice.RefundInvoice;
import com.smartstay.smartstay.repositories.CreditDebitNotesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class CreditDebitNoteService {

    @Autowired
    private Authentication authentication;
    @Autowired
    private CreditDebitNotesRepository creditDebitRepository;

    public void cancelBooking(CancelBookingDto cancelBooking) {
        if (!authentication.isAuthenticated()) {
            return;
        }

        CreditDebitNotes cdn = new CreditDebitNotes();
        cdn.setCreditedTo(cancelBooking.customerId());
        cdn.setCustomerId(cancelBooking.customerId());
        cdn.setType(CreditDebitNotesType.DEBIT.name());
        cdn.setSource(CreditDebitNoteSource.CANCEL_BOOKING.name());
        cdn.setAmount(cancelBooking.amount());
        cdn.setInvoiceId(cancelBooking.invoiceId());
        cdn.setBookingId(cancelBooking.bankId());
        cdn.setReferenceNumber(cancelBooking.referenceNumber());
        cdn.setCreatedBy(authentication.getName());
        cdn.setCreatedAt(new Date());



        creditDebitRepository.save(cdn);

    }

    public void refunInvoice(String invoiceId, InvoicesV1 invoicesV1, RefundInvoice refundInvoice) {
        if (!authentication.isAuthenticated()) {
            return;
        }

        String source = null;
        if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.SETTLEMENT.name())) {
            source = CreditDebitNoteSource.FINAL_SETTLEMT.name();
        }
        else if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.RENT.name())) {
            source = CreditDebitNoteSource.INVOICE_REFUND.name();
        }
        else if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.ADVANCE.name())) {
            source = CreditDebitNoteSource.ADVANCE.name();
        }

        CreditDebitNotes cdn = new CreditDebitNotes();
        cdn.setCreditedTo(invoicesV1.getCustomerId());
        cdn.setCustomerId(invoicesV1.getCustomerId());
        cdn.setType(CreditDebitNotesType.DEBIT.name());
        cdn.setSource(source);
        cdn.setAmount(refundInvoice.refundAmount());
        cdn.setInvoiceId(invoicesV1.getInvoiceId());
        cdn.setBookingId(refundInvoice.bankId());
        cdn.setReferenceNumber(refundInvoice.referenceNumber());
        cdn.setCreatedBy(authentication.getName());
        cdn.setCreatedAt(new Date());



        creditDebitRepository.save(cdn);
    }

    public Double getRefundedAmount(String invoiceId) {
        return creditDebitRepository.findByInvoiceId(invoiceId)
                .stream()
                .mapToDouble(i -> {
                    if (i.getAmount() == null) {
                        return 0.0;
                    }
                    return i.getAmount();
                })
                .sum();
    }
}
