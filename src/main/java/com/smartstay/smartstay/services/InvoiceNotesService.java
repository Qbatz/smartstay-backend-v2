package com.smartstay.smartstay.services;

import com.smartstay.smartstay.dao.InvoiceNotes;
import com.smartstay.smartstay.repositories.InvoiceNotesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InvoiceNotesService {
    @Autowired
    private InvoiceNotesRepository invoiceNotesRepository;

    public void addNotes(String customerId, String hostelId, String invoiceId, String description, String detailedDescription) {
        InvoiceNotes in = new InvoiceNotes();
        in.setInvoiceId(invoiceId);
        in.setHostelId(hostelId);
        in.setCustomerId(customerId);
        in.setNotes(detailedDescription);
        in.setDescription(description);

        invoiceNotesRepository.save(in);
    }
}
