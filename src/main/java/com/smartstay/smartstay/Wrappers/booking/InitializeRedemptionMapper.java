package com.smartstay.smartstay.Wrappers.booking;

import com.smartstay.smartstay.dao.Customers;
import com.smartstay.smartstay.dao.InvoicesV1;
import com.smartstay.smartstay.dto.beds.BedDetails;
import com.smartstay.smartstay.ennum.InvoiceType;
import com.smartstay.smartstay.responses.bookings.AdvanceInfo;
import com.smartstay.smartstay.responses.bookings.CustomerInfo;
import com.smartstay.smartstay.responses.bookings.InitializeInvoiceItems;
import com.smartstay.smartstay.responses.bookings.InitializeRedemption;
import com.smartstay.smartstay.util.NameUtils;
import com.smartstay.smartstay.util.Utils;
import org.springframework.security.core.parameters.P;

import java.util.function.Function;

public class InitializeRedemptionMapper implements Function<InvoicesV1, InitializeInvoiceItems> {

    @Override
    public InitializeInvoiceItems apply(InvoicesV1 invoicesV1) {
        double pendingAmount = 0;

        if (invoicesV1.getPaidAmount() != null) {
            pendingAmount = invoicesV1.getTotalAmount() - invoicesV1.getPaidAmount();
        }
        else {
            pendingAmount = invoicesV1.getTotalAmount();
        }

        return new InitializeInvoiceItems(invoicesV1.getInvoiceType(),
                invoicesV1.getInvoiceNumber(),
                Utils.dateToString(invoicesV1.getInvoiceDueDate()),
                Utils.dateToString(invoicesV1.getInvoiceStartDate()),
                invoicesV1.getTotalAmount(),
                pendingAmount,
                invoicesV1.getInvoiceId());
    }
}
