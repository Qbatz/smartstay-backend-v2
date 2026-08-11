package com.smartstay.smartstay.Wrappers.retainer;

import com.smartstay.smartstay.dao.InvoicesV1;
import com.smartstay.smartstay.dto.customer.RetainerListItems;
import com.smartstay.smartstay.ennum.InvoiceType;
import com.smartstay.smartstay.util.Utils;

import java.util.function.Function;

public class InvoiceRetainerItemsMapper implements Function<InvoicesV1, RetainerListItems>  {
    @Override
    public RetainerListItems apply(InvoicesV1 invoicesV1) {
        String status = null;

        Double invoiceTotalAmount = 0.0;
        if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.ADVANCE.name())) {
            if (invoicesV1.getDeductionAmount() != null) {
                if (invoicesV1.getDeductionAmount() > 0) {
                    invoiceTotalAmount = invoicesV1.getTotalAmount() - invoicesV1.getDeductionAmount();
                }
            }
        }
        else {
            invoiceTotalAmount = invoicesV1.getTotalAmount();
        }

        if (invoicesV1.getBalanceAmount() != null) {
            if (invoicesV1.getBalanceAmount() > 0) {
                if (invoicesV1.getBalanceAmount() < invoicesV1.getPaidAmount()) {
                    status = "Partially Adjusted";
                }
                else {
                    status = "Available";
                }
            }
            else {
                if (invoicesV1.getPaidAmount() > 0) {
                    status = "Fully Adjusted";
                }
                else {
                    status = "Not available";
                }
            }
        }
        return new RetainerListItems(invoicesV1.getInvoiceId(),
                invoicesV1.getInvoiceNumber(),
                status,
                Utils.dateToString(invoicesV1.getInvoiceStartDate()),
                invoicesV1.getInvoiceType(),
                invoiceTotalAmount,
                invoicesV1.getBalanceAmount(),
                "Cash");
    }
}
