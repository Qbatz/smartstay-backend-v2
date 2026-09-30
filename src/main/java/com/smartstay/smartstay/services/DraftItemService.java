package com.smartstay.smartstay.services;

import com.smartstay.smartstay.dao.DraftItems;
import com.smartstay.smartstay.dao.InvoiceDrafts;
import com.smartstay.smartstay.ennum.InvoiceItems;
import com.smartstay.smartstay.payloads.invoiceDrafts.AddDraftItems;
import com.smartstay.smartstay.payloads.invoiceDrafts.UpdateDraft;
import com.smartstay.smartstay.repositories.DraftItemsRepository;
import com.smartstay.smartstay.repositories.InvoiceDraftsRepositories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DraftItemService {
    @Autowired
    private DraftItemsRepository draftItemsRepository;

    public double removeItem(Long itemId) {
        DraftItems di = draftItemsRepository.findById(itemId).orElse(null);
        if (di == null) {
            return -123.4567890;
        }
        if (di.getInvoiceItem().equalsIgnoreCase(InvoiceItems.RENT.name())) {
            return -1;
        }
        double itemAmount = di.getAmount();
        draftItemsRepository.delete(di);
        return itemAmount;
    }

    public double updateDraftAmount(Long itemId, UpdateDraft updateDraft) {
        DraftItems di = draftItemsRepository.findById(itemId).orElse(null);
        if (di == null) {
            return -123.4567890;
        }
        double existingAmount = di.getAmount();
        double balanceAmount = 0.0;
        if (updateDraft.draftAmount() != null) {
            balanceAmount =  updateDraft.draftAmount() - existingAmount;
            di.setAmount(updateDraft.draftAmount());
        }
        if (updateDraft.name() != null && !updateDraft.name().isEmpty()) {
            if (updateDraft.draftAmount() == null) {
                balanceAmount = 0;
            }
            if (updateDraft.name().equalsIgnoreCase(InvoiceItems.EB.name())) {
                di.setInvoiceItem(InvoiceItems.EB.name());
            }
            else if (updateDraft.name().equalsIgnoreCase(InvoiceItems.AMENITY.name())) {
                di.setInvoiceItem(InvoiceItems.AMENITY.name());
            }
            else if (updateDraft.name().equalsIgnoreCase(InvoiceItems.MAINTENANCE.name())) {
                di.setInvoiceItem(InvoiceItems.MAINTENANCE.name());
            }
            else {
                di.setInvoiceItem(InvoiceItems.OTHERS.name());
                di.setOtherItem(updateDraft.name());
            }

        }

        return balanceAmount;
    }

    public double addItemToExistingInvoice(InvoiceDrafts invoiceDrafts, List<AddDraftItems> draftItems) {
        List<DraftItems> invoiceDraftItems = draftItems
                .stream()
                .map(i -> {
                    DraftItems di = new DraftItems();
                    di.setInvoiceDrafts(invoiceDrafts);
                    di.setAmount(i.amount());
                    if (i.name().equalsIgnoreCase(InvoiceItems.EB.name())) {
                        di.setInvoiceItem(InvoiceItems.EB.name());
                    }
                    else if (i.name().equalsIgnoreCase(InvoiceItems.AMENITY.name())) {
                        di.setInvoiceItem(InvoiceItems.AMENITY.name());
                    }
                    else if (i.name().equalsIgnoreCase(InvoiceItems.MAINTENANCE.name())) {
                        di.setInvoiceItem(InvoiceItems.MAINTENANCE.name());
                    }
                    else if (i.name().equalsIgnoreCase(InvoiceItems.RENT.name())) {
                        di.setInvoiceItem(InvoiceItems.RENT.name());
                    }
                    else {
                        di.setInvoiceItem(InvoiceItems.OTHERS.name());
                        di.setOtherItem(i.name());
                    }

                    return di;
                })
                .toList();

        draftItemsRepository.saveAll(invoiceDraftItems);

        return draftItems
                .stream()
                .mapToDouble(AddDraftItems::amount)
                .sum();

    }
}
