package com.smartstay.smartstay.services;

import com.smartstay.smartstay.dao.InvoiceItems;
import com.smartstay.smartstay.repositories.InvoiceItemsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InvoiceItemService {

    @Autowired
    private InvoiceItemsRepository invoiceItemsRepository;

    public InvoiceItems updateInvoiceItems(InvoiceItems invoiceItems) {
        return invoiceItemsRepository.save(invoiceItems);
    }
}
