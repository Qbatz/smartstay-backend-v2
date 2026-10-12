package com.smartstay.smartstay.services;

import com.smartstay.smartstay.dao.DraftItems;
import com.smartstay.smartstay.dao.InvoiceDrafts;
import com.smartstay.smartstay.ennum.InvoiceItems;
import com.smartstay.smartstay.payloads.invoiceDrafts.AddDraftItems;
import com.smartstay.smartstay.payloads.invoiceDrafts.UpdateDraft;
import com.smartstay.smartstay.repositories.InvoiceDraftsRepositories;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class InvoiceDraftsService {
    @Autowired
    private InvoiceDraftsRepositories invoiceDraftsRepositories;
    @Autowired
    private DraftItemService draftItemService;

    public InvoiceDrafts saveFromRecurring(InvoiceDrafts id) {
        return invoiceDraftsRepositories.save(id);
    }

    public List<InvoiceDrafts> getAvailableInvoice(String hostelId) {
        List<InvoiceDrafts> listInvoiceDrafts = invoiceDraftsRepositories.findByHostelId(hostelId);
        if (listInvoiceDrafts == null) {
            listInvoiceDrafts = new ArrayList<>();
        }
        return listInvoiceDrafts;
    }

    public ResponseEntity<?> deleteItemFromDraftInvoice(Long invoiceId, Long itemId) {
        InvoiceDrafts invoiceDrafts = invoiceDraftsRepositories.findById(invoiceId).orElse(null);
        if (invoiceDrafts == null) {
            return new ResponseEntity<>(Utils.INVALID_INVOICE_ID, HttpStatus.BAD_REQUEST);
        }

        double deletedAmount = draftItemService.removeItem(itemId);
        if (deletedAmount == -1) {
            return new ResponseEntity<>(Utils.CANNOT_DELETE_RENT_AMOUNT, HttpStatus.BAD_REQUEST);
        }
        if (deletedAmount == -123.4567890) {
            return new ResponseEntity<>(Utils.INVALID_INVOICE_ITEMS_ID, HttpStatus.BAD_REQUEST);
        }
        Double totalAmount = invoiceDrafts.getTotalAmount();
        if (totalAmount != null) {
            totalAmount = totalAmount - deletedAmount;
            invoiceDrafts.setTotalAmount(totalAmount);
        }
        Double basePrice = invoiceDrafts.getBasePrice();
        if (basePrice != null) {
            basePrice = basePrice - deletedAmount;
            invoiceDrafts.setBasePrice(basePrice);
        }

        Double subTotal = invoiceDrafts.getSubTotal();
        if (subTotal != null) {
            subTotal = subTotal - deletedAmount;
            invoiceDrafts.setSubTotal(subTotal);
        }

        InvoiceDrafts id = invoiceDraftsRepositories.save(invoiceDrafts);
        return new ResponseEntity<>(id.getTotalAmount(), HttpStatus.NO_CONTENT);
    }

    public ResponseEntity<?> updateDraftAmount(Long invoiceId, Long itemId, UpdateDraft updateDraft) {
        InvoiceDrafts invoiceDrafts = invoiceDraftsRepositories.findById(invoiceId).orElse(null);
        if (invoiceDrafts == null) {
            return new ResponseEntity<>(Utils.INVALID_INVOICE_ID, HttpStatus.BAD_REQUEST);
        }

        double balance = draftItemService.updateDraftAmount(itemId, updateDraft);
        if (balance == -123.4567890) {
            return new ResponseEntity<>(Utils.INVALID_INVOICE_ITEMS_ID, HttpStatus.BAD_REQUEST);
        }
        double  totalAmount = 0.0;
        if (invoiceDrafts.getTotalAmount() != null) {
            totalAmount = invoiceDrafts.getTotalAmount();
            invoiceDrafts.setTotalAmount(invoiceDrafts.getTotalAmount() + balance);
        }

        Double basePrice = invoiceDrafts.getBasePrice();
        if (basePrice != null) {
            basePrice = basePrice + balance;
            invoiceDrafts.setBasePrice(basePrice);
        }

        Double subTotal = invoiceDrafts.getSubTotal();
        if (subTotal != null) {
            subTotal = subTotal + balance;
            invoiceDrafts.setSubTotal(subTotal);
        }

        invoiceDrafts.setEdited(true);
        InvoiceDrafts id = invoiceDraftsRepositories.save(invoiceDrafts);
        return new ResponseEntity<>(id.getTotalAmount(), HttpStatus.OK);
    }

    public ResponseEntity<?> addNewItemToDraft(Long invoiceId, List<AddDraftItems> draftItems) {
        InvoiceDrafts invoiceDrafts = invoiceDraftsRepositories.findById(invoiceId).orElse(null);
        if (invoiceDrafts == null) {
            return new ResponseEntity<>(Utils.INVALID_INVOICE_ID, HttpStatus.BAD_REQUEST);
        }
        boolean hasRent = draftItems
                .stream()
                .anyMatch(i -> i.name().equalsIgnoreCase(InvoiceItems.RENT.name()));
        if (hasRent) {
            boolean alreadyExist = invoiceDrafts
                    .getListItems()
                    .stream()
                    .anyMatch(i -> i.getInvoiceItem().equalsIgnoreCase(InvoiceItems.RENT.name()));
            if (alreadyExist) {
                return new ResponseEntity<>(Utils.RENT_ALREADY_EXIST, HttpStatus.BAD_REQUEST);
            }
        }

        double additionalAmount = draftItemService.addItemToExistingInvoice(invoiceDrafts, draftItems);

        double  totalAmount = 0.0;
        if (invoiceDrafts.getTotalAmount() != null) {
            totalAmount = invoiceDrafts.getTotalAmount();
            invoiceDrafts.setTotalAmount(invoiceDrafts.getTotalAmount() + additionalAmount);
        }

        Double basePrice = invoiceDrafts.getBasePrice();
        if (basePrice != null) {
            basePrice = basePrice + additionalAmount;
            invoiceDrafts.setBasePrice(basePrice);
        }

        Double subTotal = invoiceDrafts.getSubTotal();
        if (subTotal != null) {
            subTotal = subTotal + additionalAmount;
            invoiceDrafts.setSubTotal(subTotal);
        }

        invoiceDrafts.setEdited(true);
        InvoiceDrafts id = invoiceDraftsRepositories.save(invoiceDrafts);
        return new ResponseEntity<>(id.getTotalAmount(), HttpStatus.OK);
    }

    public List<InvoiceDrafts> getAllDraftedInvoices(String hostelId, List<Long> invoiceIds) {
        List<InvoiceDrafts> listInvoiceDraft = invoiceDraftsRepositories.findByHostelIdAndDraftIdIn(hostelId, invoiceIds);
        if (listInvoiceDraft == null) {
            return new ArrayList<>();
        }
        return listInvoiceDraft;

    }

    public void deleteGeneratedInvoices(String hostelId, List<Long> invoicesIdsToDelete) {
        List<InvoiceDrafts> listInvoiceDrafts = invoiceDraftsRepositories.findByHostelIdAndDraftIdIn(hostelId, invoicesIdsToDelete);
        invoiceDraftsRepositories.deleteAll(listInvoiceDrafts);
    }
}
