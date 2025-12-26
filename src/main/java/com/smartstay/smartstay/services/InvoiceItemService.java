package com.smartstay.smartstay.services;

import com.smartstay.smartstay.dao.InvoiceItems;
import com.smartstay.smartstay.dao.InvoicesV1;
import com.smartstay.smartstay.payloads.invoice.UpdateRecurringInvoice;
import com.smartstay.smartstay.repositories.InvoiceItemsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class InvoiceItemService {

    @Autowired
    private InvoiceItemsRepository invoiceItemsRepository;

    public InvoiceItems updateInvoiceItems(InvoiceItems invoiceItems) {
        return invoiceItemsRepository.save(invoiceItems);
    }

    public void updateInvoiceItems(List<InvoiceItems> invoiceItems) {
        invoiceItemsRepository.saveAll(invoiceItems);
    }

    public Double updateRecurringInvoiceItems(List<UpdateRecurringInvoice> recurringInvoiceItems, InvoicesV1 invoicesV1) {
        List<InvoiceItems> registeredInvoiceItems = invoicesV1.getInvoiceItems();
        List<InvoiceItems> newUpdatedItem = new ArrayList<>();
        InvoiceItems ebItem = registeredInvoiceItems
                .stream()
                .filter(i -> i.getInvoiceItem().equalsIgnoreCase(com.smartstay.smartstay.ennum.InvoiceItems.EB.name()))
                .findFirst()
                .orElse(null);
        if (ebItem == null) {
            InvoiceItems newEBItem = recurringInvoiceItems
                    .stream()
                    .filter(i -> i.type().equalsIgnoreCase(com.smartstay.smartstay.ennum.InvoiceItems.EB.name()))
                    .map(i -> {
                        InvoiceItems ii = new InvoiceItems();
                        ii.setAmount(i.amount());
                        ii.setInvoiceItem(i.type());
                        ii.setInvoice(invoicesV1);

                        return ii;
                    })
                    .findFirst()
                    .orElse(null);
            if (newEBItem != null) {
                newUpdatedItem.add(newEBItem);
            }
        }
        else {
            InvoiceItems updateEbItems = recurringInvoiceItems
                    .stream()
                    .filter(i -> i.type().equalsIgnoreCase(com.smartstay.smartstay.ennum.InvoiceItems.EB.name()))
                    .map(i -> {
                        ebItem.setAmount(i.amount());

                        return ebItem;
                    })
                    .findFirst()
                    .orElse(null);

            newUpdatedItem.add(updateEbItems);
        }

        List<InvoiceItems> otherItems = new ArrayList<>(recurringInvoiceItems
                .stream()
                .filter(i -> {
                    return (!i.type().equalsIgnoreCase(com.smartstay.smartstay.ennum.InvoiceItems.EB.name())) &&
                            (!i.type().equalsIgnoreCase(com.smartstay.smartstay.ennum.InvoiceItems.RENT.name()));
                })
                .map(im -> {
                    InvoiceItems imItm = new InvoiceItems();
                    imItm.setInvoiceItem(com.smartstay.smartstay.ennum.InvoiceItems.OTHERS.name());
                    imItm.setOtherItem(im.type());
                    imItm.setAmount(im.amount());
                    imItm.setInvoice(invoicesV1);

                    return imItm;
                })
                .toList());

        otherItems.addAll(newUpdatedItem);

        invoiceItemsRepository.saveAll(otherItems);

        List<InvoiceItems> newInvItm = invoiceItemsRepository.findByInvoice_InvoiceId(invoicesV1.getInvoiceId());

        double newAmount = newInvItm
                .stream()
                .mapToDouble(InvoiceItems::getAmount)
                .sum();

        return newAmount;
    }
}
