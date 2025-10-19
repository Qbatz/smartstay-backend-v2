package com.smartstay.smartstay.Wrappers.invoices;

import com.smartstay.smartstay.dao.InvoicesV1;
import com.smartstay.smartstay.dto.transaction.PartialPaidInvoiceInfo;
import com.smartstay.smartstay.ennum.InvoiceItems;
import com.smartstay.smartstay.ennum.InvoiceType;
import com.smartstay.smartstay.responses.customer.UnpaidInvoices;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class UnpaidInvoicesMapper implements Function<InvoicesV1, UnpaidInvoices> {

    List<PartialPaidInvoiceInfo> listPartialPayments = null;

    public UnpaidInvoicesMapper(List<PartialPaidInvoiceInfo> listPartialPayments) {
        this.listPartialPayments = listPartialPayments;
    }

    @Override
    public UnpaidInvoices apply(InvoicesV1 invoicesV1) {

        String invoiceType = null;
        double invoicePendingAmount = 0.0;
        Double ebAmount = 0.0;
        Double amenityAmount = 0.0;

        if (listPartialPayments != null && !listPartialPayments.isEmpty()) {
            Double paidAmount = listPartialPayments
                    .stream()
                    .filter(item -> Objects.equals(item.invoiceId(), invoicesV1.getInvoiceId()))
                    .mapToDouble(PartialPaidInvoiceInfo::paidAmount)
                    .sum();
            invoicePendingAmount = invoicesV1.getTotalAmount() - paidAmount;
        }
        else {
            invoicePendingAmount = invoicesV1.getTotalAmount();
        }

        ebAmount = invoicesV1
                .getInvoiceItems()
                .stream()
                .filter(item ->  item.getInvoiceItem().equalsIgnoreCase(InvoiceItems.EB.name()))
                .mapToDouble(com.smartstay.smartstay.dao.InvoiceItems::getAmount)
                .sum();

        if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.RENT.name())) {
            invoiceType = "Rent";
        }
        else if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.ADVANCE.name())) {
            invoiceType = "Advance";
        }

        return  new UnpaidInvoices(invoicesV1.getInvoiceNumber(),
                invoicesV1.getInvoiceId(),
                invoiceType,
                invoicesV1.getTotalAmount(),
                invoicePendingAmount,
                ebAmount,
                amenityAmount
                );
    }
}
