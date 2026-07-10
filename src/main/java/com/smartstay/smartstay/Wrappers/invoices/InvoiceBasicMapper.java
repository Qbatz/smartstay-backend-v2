package com.smartstay.smartstay.Wrappers.invoices;

import com.smartstay.smartstay.dao.Customers;
import com.smartstay.smartstay.dao.InvoicesV1;
import com.smartstay.smartstay.responses.invoices.InvoiceBasicList;
import com.smartstay.smartstay.util.CustomerUtils;
import com.smartstay.smartstay.util.NameUtils;
import com.smartstay.smartstay.util.Utils;

import java.util.List;
import java.util.function.Function;

public class InvoiceBasicMapper implements Function<InvoicesV1, InvoiceBasicList> {
    private List<Customers> listCustomers = null;

    public InvoiceBasicMapper(List<Customers> listCustomers) {
        this.listCustomers = listCustomers;
    }

    @Override
    public InvoiceBasicList apply(InvoicesV1 invoicesV1) {
        String customerName = null;
        String profilePic = null;
        String initials = null;

        if (listCustomers != null) {
            Customers customers = listCustomers.stream()
                    .filter(i -> i.getCustomerId().equalsIgnoreCase(invoicesV1.getCustomerId()))
                    .findFirst()
                    .orElse(null);
            if (customers != null) {
                customerName = NameUtils.getFullName(customers.getFirstName(), customers.getLastName());
                profilePic = CustomerUtils.getProfilePic(customers);
                initials = NameUtils.getInitials(customers.getFirstName(), customers.getLastName());
            }
        }




        return new InvoiceBasicList(customerName,
                invoicesV1.getInvoiceId(),
                profilePic,
                initials,
                invoicesV1.getInvoiceNumber(),
                invoicesV1.getPaymentStatus(),
                invoicesV1.getTotalAmount(),
                Utils.dateToString(invoicesV1.getInvoiceStartDate()));
    }
}
