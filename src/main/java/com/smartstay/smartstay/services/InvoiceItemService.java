package com.smartstay.smartstay.services;

import com.smartstay.smartstay.dao.InvoiceItems;
import com.smartstay.smartstay.dao.InvoicesV1;
import com.smartstay.smartstay.ennum.InvoiceType;
import com.smartstay.smartstay.payloads.invoice.UpdateRecurringInvoice;
import com.smartstay.smartstay.repositories.InvoiceItemsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

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
            double ebAmount = recurringInvoiceItems
                    .stream()
                    .filter(i -> i.type().equalsIgnoreCase(com.smartstay.smartstay.ennum.InvoiceItems.EB.name()))
                    .mapToDouble(UpdateRecurringInvoice::amount)
                    .sum();
            if (ebAmount > 0) {
                InvoiceItems ii = new InvoiceItems();
                ii.setAmount(ebAmount);
                ii.setInvoiceItem(com.smartstay.smartstay.ennum.InvoiceItems.EB.name());
                ii.setInvoice(invoicesV1);

                newUpdatedItem.add(ii);
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
            if (updateEbItems != null) {
                newUpdatedItem.add(updateEbItems);
            }
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

    public double updateInvoiceItems(List<UpdateRecurringInvoice> recurringInvoiceItems, List<InvoiceItems> invoiceItems, InvoicesV1 invoiceV1) {
        if (invoiceV1.getInvoiceType().equalsIgnoreCase(InvoiceType.ADVANCE.name())) {
            double deductionAmount = 0.0;
            AtomicReference<Double> totalAmount = new AtomicReference<>(0.0);
            if (invoiceV1.getDeductions() != null) {
                deductionAmount = invoiceV1.getDeductionAmount();
            }

            List<InvoiceItems> invItems = new ArrayList<>();

            recurringInvoiceItems.forEach(item -> {
                InvoiceItems items = invoiceItems
                        .stream()
                        .filter(i -> i.getInvoiceItem().equalsIgnoreCase(item.type()))
                        .findFirst()
                        .orElse(null);
                if (items != null) {
                    items.setAmount(item.amount());
                    invItems.add(items);
                    totalAmount.set(totalAmount.get() + item.amount());
                }
            });

            if (!invItems.isEmpty()) {
                invoiceItemsRepository.saveAll(invItems);
            }
            return totalAmount.get() + deductionAmount;
        }
        else {
            AtomicReference<Double> newInvoiceAmount = new AtomicReference<>(0.0);
            List<InvoiceItems> newInvoiceItemsToupdate = new ArrayList<>();
            if (recurringInvoiceItems != null) {
                recurringInvoiceItems.forEach(item -> {
                    InvoiceItems invItem = invoiceItems
                            .stream()
                            .filter(i -> i.getInvoiceItem().equalsIgnoreCase(item.type()))
                            .findFirst()
                            .orElse(null);

                    if (invItem != null) {
                        newInvoiceAmount.set(newInvoiceAmount.get() + item.amount());
                        invItem.setAmount(item.amount());

                        newInvoiceItemsToupdate.add(invItem);
                    }
                    else {
                        InvoiceItems invOtherItems = invoiceItems
                                .stream()
                                .filter(i -> i.getOtherItem() != null && i.getOtherItem().equalsIgnoreCase(item.type()))
                                .findFirst()
                                .orElse(null);

                        if (invOtherItems != null) {
                            newInvoiceAmount.set(newInvoiceAmount.get() + item.amount());
                            invOtherItems.setAmount(item.amount());
                            newInvoiceItemsToupdate.add(invOtherItems);
                        }
                        else {
                            InvoiceItems invItems = new InvoiceItems();
                            invItems.setAmount(item.amount());
                            if (item.type().equalsIgnoreCase(com.smartstay.smartstay.ennum.InvoiceItems.RENT.name())) {
                                invItems.setInvoiceItem(com.smartstay.smartstay.ennum.InvoiceItems.RENT.name());
                            }
                            else if (item.type().equalsIgnoreCase(com.smartstay.smartstay.ennum.InvoiceItems.AMENITY.name())) {
                                invItems.setInvoiceItem(com.smartstay.smartstay.ennum.InvoiceItems.AMENITY.name());
                            }
                            else if (item.type().equalsIgnoreCase(com.smartstay.smartstay.ennum.InvoiceItems.EB.name())) {
                                invItems.setInvoiceItem(com.smartstay.smartstay.ennum.InvoiceItems.EB.name());
                            }
                            else {
                                invItems.setInvoiceItem(com.smartstay.smartstay.ennum.InvoiceItems.OTHERS.name());
                                invItems.setOtherItem(item.type());
                            }


                            invItems.setInvoice(invoiceV1);
                            newInvoiceItemsToupdate.add(invItems);

                            newInvoiceAmount.set(newInvoiceAmount.get() + item.amount());

                        }
                    }
                });

                if (!newInvoiceItemsToupdate.isEmpty()) {
                    invoiceItemsRepository.saveAll(newInvoiceItemsToupdate);
                }
            }

            return newInvoiceAmount.get();
        }

    }

}
