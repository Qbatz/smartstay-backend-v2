package com.smartstay.smartstay.Wrappers.customers;

import com.smartstay.smartstay.dao.InvoicesV1;
import com.smartstay.smartstay.responses.customer.RentBreakUp;
import com.smartstay.smartstay.util.Utils;

import java.util.function.Function;

public class FinalSettlementMapper implements Function<InvoicesV1, RentBreakUp> {
    @Override
    public RentBreakUp apply(InvoicesV1 invoicesV1) {
        double rentAmount = invoicesV1.getTotalAmount();
        long noOfDays = Utils.findNumberOfDays(invoicesV1.getInvoiceStartDate(), invoicesV1.getInvoiceEndDate());

        return new RentBreakUp(Utils.dateToString(invoicesV1.getInvoiceStartDate()),
                Utils.dateToString(invoicesV1.getInvoiceEndDate()),
                noOfDays,
                rentAmount);
    }
}
