package com.smartstay.smartstay.Wrappers.retainer;

import com.smartstay.smartstay.dao.InvoicesV1;
import com.smartstay.smartstay.dto.retainer.RetainerItems;
import com.smartstay.smartstay.util.Utils;

import java.util.Date;
import java.util.function.Function;

public class InvoiceRetainerItemMapper implements Function<InvoicesV1, RetainerItems> {
    @Override
    public RetainerItems apply(InvoicesV1 invoicesV1) {
        double redeemedAmount = 0.0;
        double availableAmount = 0.0;
        double invoiceAmount = 0.0;
        Date invoiceDate = invoicesV1.getInvoiceStartDate();
        if (invoicesV1.getInvoiceDate() != null) {
            invoiceDate = invoicesV1.getInvoiceDate();
        }
        if (invoicesV1.getTotalAmount() != null) {
            invoiceAmount = invoicesV1.getTotalAmount();
        }
        if (invoicesV1.getBalanceAmount() != null) {
            availableAmount = invoicesV1.getBalanceAmount();
        }

        redeemedAmount = invoiceAmount - availableAmount;

        return new RetainerItems(invoicesV1.getInvoiceId(),
                invoicesV1.getInvoiceNumber(),
                Utils.dateToString(invoiceDate),
                Utils.dateToString(invoiceDate),
                Utils.roundOffWithTwoDigit(invoicesV1.getTotalAmount()),
                redeemedAmount,
                availableAmount);
    }
}
