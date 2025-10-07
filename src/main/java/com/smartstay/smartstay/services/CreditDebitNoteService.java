package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.CreditDebitNotes;
import com.smartstay.smartstay.dto.customer.CancelBookingDto;
import com.smartstay.smartstay.ennum.CreditDebitNotesType;
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
        cdn.setAmount(cancelBooking.amount());
        cdn.setInvoiceId(cancelBooking.invoiceId());
        cdn.setBookingId(cancelBooking.bankId());
        cdn.setReferenceNumber(cancelBooking.referenceNumber());
        cdn.setCreatedBy(authentication.getName());
        cdn.setCreatedAt(new Date());



        creditDebitRepository.save(cdn);

    }
}
