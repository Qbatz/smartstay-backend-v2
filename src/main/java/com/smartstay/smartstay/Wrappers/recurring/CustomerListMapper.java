package com.smartstay.smartstay.Wrappers.recurring;

import com.smartstay.smartstay.dao.Customers;
import com.smartstay.smartstay.dao.CustomersConfig;
import com.smartstay.smartstay.dao.InvoicesV1;
import com.smartstay.smartstay.responses.recurring.CustomersList;
import com.smartstay.smartstay.util.Utils;

import java.util.Date;
import java.util.List;
import java.util.function.Function;

public class CustomerListMapper implements Function<Customers, CustomersList> {

    List<CustomersConfig> customersConfigList = null;
    List<InvoicesV1> listOfLatestInvoices = null;

    String nextInvoiceDate = null;

    public CustomerListMapper(List<CustomersConfig> customersConfigList, List<InvoicesV1> listInvoices, Date nextInvoiceDate) {
        this.customersConfigList = customersConfigList;
        this.listOfLatestInvoices = listInvoices;
        this.nextInvoiceDate = Utils.dateToString(nextInvoiceDate);
    }

    @Override
    public CustomersList apply(Customers customers) {
        StringBuilder initials = new StringBuilder();
        StringBuilder fullName = new StringBuilder();
        String lastInvoiceNumber = null;
        String lastInvoiceDate = null;
        Double lastInvoiceAmount = 0.0;

        boolean isEnabled = false;

        CustomersConfig customersConfig = customersConfigList
                .stream()
                .filter(i -> i.getCustomerId().equalsIgnoreCase(customers.getCustomerId()))
                .findFirst()
                .orElse(null);
        if (customersConfig != null) {
            isEnabled = customersConfig.getEnabled();
        }

        if (customers.getFirstName() != null) {
            initials.append(customers.getFirstName().toUpperCase().charAt(0));
            fullName.append(customers.getFirstName());
        }
        if (customers.getLastName() != null && !customers.getLastName().equalsIgnoreCase("")) {
            initials.append(customers.getLastName().toUpperCase().charAt(0));
            fullName.append(" ");
            fullName.append(customers.getLastName());
        }
        else if (customers.getFirstName() != null){
            if (customers.getFirstName().length() > 1) {
                initials.append(customers.getFirstName().toUpperCase().charAt(1));
            }
        }

        InvoicesV1 latestInvoice = listOfLatestInvoices
                .stream()
                .filter(i -> i.getCustomerId().equalsIgnoreCase(customers.getCustomerId()))
                .findFirst()
                .orElse(null);

        if (latestInvoice != null) {
            lastInvoiceAmount = latestInvoice.getTotalAmount();
            lastInvoiceDate = Utils.dateToString(latestInvoice.getInvoiceStartDate());
            lastInvoiceNumber = latestInvoice.getInvoiceNumber();
        }

        return new CustomersList(customers.getCustomerId(),
                customers.getFirstName(),
                customers.getLastName(),
                fullName.toString(),
                initials.toString(),
                customers.getProfilePic(),
                isEnabled,
                lastInvoiceNumber,
                lastInvoiceDate,
                nextInvoiceDate,
                lastInvoiceAmount);
    }
}
